package org.dhis2.usescases.enrollment

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
import org.dhis2.data.mapping.MappingService
import org.dhis2.form.model.RowAction
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.DELETE_AND_BACK
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import timber.log.Timber
import java.util.Calendar.DAY_OF_YEAR
import java.util.Date

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
    mappingService: MappingService,
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
                    enrollmentFormRepository.generateEvents()
                        .defaultSubscribe(
                            schedulerProvider,
                            {
                                it.second?.let { eventUid ->
                                    view.openEvent(eventUid)
                                } ?: view.openDashboard(it.first)
                            },
                            { Timber.tag(TAG).e(it) },
                        ),
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
            val programStage = d2.programModule().programStages().uid(event?.programStage()).blockingGet()
            val enrollment = enrollmentObjectRepository.blockingGet()
            val generatedByEnrollment = programStage?.generatedByEnrollmentDate() ?: false
            val startDate = if (generatedByEnrollment) enrollment?.enrollmentDate() else enrollment?.incidentDate()
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
