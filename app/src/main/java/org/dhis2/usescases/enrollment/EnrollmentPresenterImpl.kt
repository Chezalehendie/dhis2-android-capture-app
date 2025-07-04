package org.dhis2.usescases.enrollment

import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.bindings.profilePicturePath
import org.dhis2.commons.bindings.trackedEntityTypeForTei
import org.dhis2.commons.data.TeiAttributesInfo
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.matomo.Actions.Companion.CREATE_TEI
import org.dhis2.commons.matomo.Categories.Companion.TRACKER_LIST
import org.dhis2.commons.matomo.Labels.Companion.CLICK
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.dhis2.form.model.RowAction
import org.dhis2.usescases.customConfigTransformation.AutoEnrollmentManager
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.DELETE_AND_BACK
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import timber.log.Timber
import java.util.Calendar
import java.util.Calendar.DAY_OF_YEAR
import java.util.Date

/**
 * Presenter class responsible for handling logic related to TEI enrollment,
 * displaying TEI attributes, managing auto-enrollment mapping logic,
 * creating events, and updating enrollment status in the DHIS2 Capture App.
 *
 * This presenter integrates auto-enrollment configurations to allow seamless
 * population of data values from related programs, minimizing duplicate data entry.
 *
 * @property view Interface for UI interaction.
 * @property d2 DHIS2 SDK core instance.
 * @property enrollmentObjectRepository Repository for the current enrollment.
 * @property teiRepository Repository for the tracked entity instance.
 * @property programRepository Repository for the selected program.
 * @property schedulerProvider Schedulers for async task execution.
 * @property enrollmentFormRepository Repository for form-level operations.
 * @property analyticsHelper Helper class for usage analytics.
 * @property matomoAnalyticsController Matomo analytics integration.
 * @property eventCollectionRepository Repository for managing events.
 * @property teiAttributesProvider Utility to retrieve TEI attribute values.
 * @property dateEditionWarningHandler Displays warnings for date-sensitive fields.
 * @property autoEnrollmentManager Handles auto-enrollment mapping and value transfer.
 */
private const val TAG = "EnrollmentPresenter"

class EnrollmentPresenterImpl(
    val view: EnrollmentView,
    val d2: D2,
    private val enrollmentObjectRepository: EnrollmentObjectRepository,
    private val teiRepository: TrackedEntityInstanceObjectRepository,
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
    private val schedulerProvider: SchedulerProvider,
    private val enrollmentFormRepository: EnrollmentFormRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val eventCollectionRepository: EventCollectionRepository,
    private val teiAttributesProvider: TeiAttributesProvider,
    private val dateEditionWarningHandler: DateEditionWarningHandler,
    private val autoEnrollmentManager: AutoEnrollmentManager, // injected auto enrollments configs
) {

    private val disposable = CompositeDisposable()
    private val backButtonProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()

    fun init() {
        view.setSaveButtonVisible(false)

        disposable.add(
            teiRepository.get()
                .map { tei ->
                    val attrList = mutableListOf<String>()
                    val attributesValues =
                        teiAttributesProvider
                            .getListOfValuesFromProgramTrackedEntityAttributesByProgram(
                                programRepository.blockingGet()?.uid() ?: "",
                                tei.uid(),
                            )
                    val teiTypeAttributeValue = mutableListOf<TrackedEntityAttributeValue>()
                    if (attributesValues.isEmpty()) {
                        teiTypeAttributeValue.addAll(
                            teiAttributesProvider.getValuesFromTrackedEntityTypeAttributes(
                                tei.trackedEntityType(),
                                tei.uid(),
                            ),
                        )
                        attrList.addAll(teiTypeAttributeValue.map { it.value() ?: "" })
                    } else {
                        attrList.addAll(attributesValues.map { it.value() ?: "" })
                    }

                    TeiAttributesInfo(
                        attributes = attrList,
                        profileImage = tei.profilePicturePath(
                            d2,
                            programRepository.blockingGet()?.uid(),
                        ),
                        teTypeName = d2.trackedEntityTypeForTei(tei.uid())?.displayName()!!,
                    )
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::displayTeiInfo,
                    Timber.tag(TAG)::e,
                ),
        )

        disposable.add(
            programRepository.get()
                .map { it.access()?.data()?.write() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setAccess(it) },
                    { Timber.tag(TAG).e(it) },
                ),
        )

        disposable.add(
            enrollmentObjectRepository.get()
                .map { it.status() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.renderStatus(it!!) },
                    { Timber.tag(TAG).e(it) },
                ),
        )


    }

    fun subscribeToBackButton() {
        disposable.add(
            backButtonProcessor
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.performSaveClick() },
                    { t -> Timber.e(t) },
                ),
        )
    }

    fun finish(enrollmentMode: EnrollmentActivity.EnrollmentMode) {

        when (enrollmentMode) {
            EnrollmentActivity.EnrollmentMode.NEW -> {
                matomoAnalyticsController.trackEvent(TRACKER_LIST, CREATE_TEI, CLICK)
                disposable.add(
                    Single.zip(
                        autoEnrollmentManager.getCustomConfigurations(),
                        enrollmentFormRepository.generateEvents(),
                        ::CustomConfigAndEvents
                    ).defaultSubscribe(
                        schedulerProvider,
                        onSuccess = { customConfigsAndEvents ->
                            val enrollmentConfig = customConfigsAndEvents.enrollmentConfig
                            Timber.tag("CHECK_ENROLLMENT")
                                .d("Enrollment Configs from data class: ${enrollmentConfig.toString()}")

                            val eventAndEnrollmentIds = customConfigsAndEvents.eventAndEnrollmentIds
                            Timber.tag("CHECK_ENROLLMENT")
                                .d("Event and Enrollment Ids: ${eventAndEnrollmentIds.toString()}")

                            val enrollmentId = eventAndEnrollmentIds.first
                            Timber.tag("CHECK_ENROLLMENT").d("Enrollment Id: $enrollmentId")

                            val mappingRule = enrollmentConfig.mappingRules.find {
                                it.targetProgram == d2.enrollmentModule()
                                    .enrollments()
                                    .uid(enrollmentId)
                                    .blockingGet()?.program()
                            }
                            Timber.tag("CHECK_ENROLLMENT")
                                .d("Mapping Rule: ${mappingRule.toString()}")

                            if (mappingRule != null) {
                                //Get current tei
                                val tei = d2.enrollmentModule()
                                    .enrollments()
                                    .uid(enrollmentId)
                                    .blockingGet()?.trackedEntityInstance()
                                Timber.tag("CHECK_ENROLLMENT").d("Tracked Entity Instance: $tei")

                                // get current org unit
                                val orgUnit = d2.enrollmentModule().enrollments()
                                    .uid(enrollmentId)
                                    .blockingGet()
                                    ?.organisationUnit()
                                Timber.tag("CHECK_ENROLLMENT").d("Org Unit: $orgUnit")

                                // Extract the data element UIDs into a list
                                val dataElementUids =
                                    mappingRule.targetProgramStages.firstOrNull()?.targetProgramStageDataElements?.map {
                                        it.targetDataElement
                                    }
                                Timber.tag("CHECK_ENROLLMENT").d("Data Elements: $dataElementUids")

                                val sourceDataElementsUids =
                                    mappingRule.sourceProgramStages.firstOrNull()?.sourceProgramStageDataElements?.map {
                                        it.sourceDataElement
                                    }
                                Timber.tag("CHECK_ENROLLMENT")
                                    .d("Source Data Elements: $sourceDataElementsUids")

                                //find latest enrollment for a tei from source program
                                val teiFromSource = d2.enrollmentModule()
                                    .enrollments().byProgram()
                                    .eq(mappingRule.sourceProgram).byTrackedEntityInstance()
                                    .eq(tei)
                                    .orderByEnrollmentDate(RepositoryScope.OrderByDirection.ASC)
                                    .blockingGet()[0]
                                Timber.tag("CHECK_ENROLLMENT")
                                    .d("Tracked Entity Instance From Source: $teiFromSource")


                                val dataValuesGroupedByProgramStages =
                                    mappingRule.sourceProgramStages.map {
                                        autoEnrollmentManager.getTrackedEntityDataValuesByProgramStageAndEnrollment(
                                            it.sourceProgramStage, teiFromSource.uid()
                                        ).filter { entry ->
                                            entry.dataElement() == "${sourceDataElementsUids?.get(0)}"
                                                    || entry.dataElement() == "${
                                                sourceDataElementsUids?.get(
                                                    1
                                                )
                                            } "
//                                                   || entry.dataElement()=="${sourceDataElementsUids?.get(2)}"
//                                                    || entry.dataElement()=="${sourceDataElementsUids?.get(3)}"
//                                                    || entry.dataElement()=="${sourceDataElementsUids?.get(4)}"
                                        }
                                    }
                                Timber.tag("CHECK_ENROLLMENT")
                                    .d("Data Values Grouped By Program Stages: ${dataValuesGroupedByProgramStages.toString()}")

                                //create event projection

                                //String enrollment, String program, String programStage, String organisationUnit, String attributeOptionCombo
                                val ep = EventCreateProjection.create(
                                    enrollmentId,
                                    mappingRule.targetProgram,
                                    mappingRule.targetProgramStages.firstOrNull()?.targetProgramStage,
                                    orgUnit,
                                    null
                                )
                                Timber.tag("CHECK_ENROLLMENT")
                                    .d("Event Projection: ${ep.toString()}")

                                val eventId = d2.eventModule().events().blockingAdd(ep)

                                val cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] = 0
                                cal[Calendar.MINUTE] = 0
                                cal[Calendar.SECOND] = 0
                                cal[Calendar.MILLISECOND] = 0

                                d2.eventModule().events().uid(eventId).setEventDate(cal.time)
                                Timber.tag("CHECK_ENROLLMENT").d(
                                    "Successfully created event: ${
                                        d2.eventModule().events().uid(eventId).blockingGet()
                                            ?.status()
                                    }"
                                )
                                try {
                                    //Transfer data values from source to new event
                                    dataValuesGroupedByProgramStages.flatten()
                                        .forEachIndexed { index, dataValue ->
                                            dataValue.dataElement()?.let {
                                                dataElementUids?.getOrNull(index)
                                                    ?.let { dataElementUid ->
                                                        d2.trackedEntityModule()
                                                            .trackedEntityDataValues()
                                                            .value(eventId, dataElementUid)
                                                            .blockingSet(dataValue.value())

                                                        Timber.tag("CHECK_ENROLLMENT")
                                                            .d("Success! Source data element: $it, Target Data Element${dataElementUid}, data value: ${dataValue.value()}")
                                                    }
                                                //Timber.tag("CHECK_ENROLLMENT").d("Success! Source data element: $it, Tagert Data Element${dataElementUids}, data value: ${dataValue.value()}")
                                            }
                                        }
//                                    if(eventId.isNotEmpty()){
//                                        view.openEvent(eventId)
//                                    }
//                                    else{
//                                        view.openDashboard(eventAndEnrollmentIds.first)
//                                    }
                                } catch (e: Exception) {
                                    if (e is D2Error) {
                                        Timber.tag("CHECK_ENROLLMENT").e(e.toString())
                                    } else {
                                        Timber.tag("CHECK_ENROLLMENT")
                                            .e(e, "Failed to create event")
                                    }
                                }
                                eventAndEnrollmentIds.second?.let { view.openEvent(it) }
                                    ?: view.openDashboard(eventAndEnrollmentIds.first)
                            } else {
                                eventAndEnrollmentIds.second?.let { view.openEvent(it) }
                                    ?: view.openDashboard(eventAndEnrollmentIds.first)
                            }

                        },
                        onError = {
                            Timber.tag("CHECK_ENROLLMENNT").e(it)
                        }
                    )
                )
            }

            EnrollmentActivity.EnrollmentMode.CHECK -> view.setResultAndFinish()
        }
    }

    fun updateFields(action: RowAction? = null) {
        action?.let {
            dateEditionWarningHandler.shouldShowWarning(fieldUid = it.id) { message ->
                view.showDateEditionWarning(message)
            }
        }
    }

    fun backIsClicked() {
        backButtonProcessor.onNext(true)
    }

    fun getEnrollment(): Enrollment? {
        return enrollmentObjectRepository.blockingGet()
    }

    fun getProgram(): Program? {
        return programRepository.blockingGet()
    }

    fun updateEnrollmentStatus(newStatus: EnrollmentStatus): Boolean {
        return try {
            if (getProgram()?.access()?.data()?.write() == true) {
                enrollmentObjectRepository.setStatus(newStatus)
                view.renderStatus(newStatus)
                true
            } else {
                view.displayMessage(null)
                false
            }
        } catch (error: D2Error) {
            false
        }
    }

    fun saveEnrollmentGeometry(geometry: Geometry?) {
        enrollmentObjectRepository.setGeometry(geometry)
    }

    fun saveTeiGeometry(geometry: Geometry?) {
        teiRepository.setGeometry(geometry)
    }

    fun deleteAllSavedData() {
        if (teiRepository.blockingGet()?.syncState() == State.TO_POST) {
            teiRepository.blockingDelete()
        } else {
            enrollmentObjectRepository.blockingDelete()
        }
        analyticsHelper.setEvent(DELETE_AND_BACK, CLICK, DELETE_AND_BACK)
    }

    fun onDettach() {
        disposable.clear()
    }

    fun displayMessage(message: String?) {
        view.displayMessage(message)
    }

    fun onTeiImageHeaderClick() {
        val picturePath = enrollmentFormRepository.getProfilePicture()
        if (picturePath.isNotEmpty()) {
            view.displayTeiPicture(picturePath)
        }
    }

    fun hasWriteAccess() = enrollmentFormRepository.hasWriteAccess()

    fun showOrHideSaveButton() {
        val teiUid = teiRepository.blockingGet()?.uid() ?: ""
        val programUid = getProgram()?.uid() ?: ""
        val hasEnrollmentAccess = d2.enrollmentModule().enrollmentService()
            .blockingGetEnrollmentAccess(teiUid, programUid)
        if (hasEnrollmentAccess == EnrollmentAccess.WRITE_ACCESS) {
            view.setSaveButtonVisible(visible = true)
        } else {
            view.setSaveButtonVisible(visible = false)
        }
    }

    fun isEventScheduleOrSkipped(eventUid: String): Boolean {
        val event = eventCollectionRepository.uid(eventUid).blockingGet()
        return event?.status() == EventStatus.SCHEDULE ||
                event?.status() == EventStatus.SKIPPED ||
                event?.status() == EventStatus.OVERDUE
    }

    fun suggestedReportDateIsNotFutureDate(eventUid: String): Boolean {
        return try {
            val event = eventCollectionRepository.uid(eventUid).blockingGet()
            val programStage =
                d2.programModule().programStages().uid(event?.programStage()).blockingGet()
            val enrollment = enrollmentObjectRepository.blockingGet()
            val generatedByEnrollment = programStage?.generatedByEnrollmentDate() ?: false
            val startDate =
                if (generatedByEnrollment) enrollment?.enrollmentDate() else enrollment?.incidentDate()
            val calendar = DateUtils.getInstance().getCalendarByDate(startDate)
            calendar.add(DAY_OF_YEAR, programStage?.minDaysFromStart() ?: 0)
            val minStartReportEventDate = calendar.time
            val currentDate = DateUtils.getInstance().getStartOfDay(Date())
            return minStartReportEventDate.before(currentDate) || minStartReportEventDate == currentDate
        } catch (e: Exception) {
            Timber.d(e.message)
            true
        }
    }
}
