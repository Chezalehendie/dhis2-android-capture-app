package org.dhis2.usescases.events

import org.dhis2.usescases.enrollment.events.ScheduledEventContract
import org.dhis2.usescases.enrollment.events.ScheduledEventPresenterImpl
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class ScheduledEventPresenterImplTest {

    lateinit var presenter: ScheduledEventPresenterImpl
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val view: ScheduledEventContract.View = mock()

    @Before
    fun setUp() {
        presenter = ScheduledEventPresenterImpl(view, d2, "eventUid")
    }

    @Test
    fun `Should open event form screen if the event needs extra info`() {
        whenever(
            d2.eventModule().events().uid(any()),
        ) doReturn mock()

        presenter.setEventDate(Date())

        verify(view).openFormActivity()
    }

    @Test
    fun `Should open event form screen if the event does not need coordinates or catCombo`() {
        whenever(
            d2.eventModule().events().uid(any()),
        ) doReturn mock()

        presenter.setEventDate(Date())

        verify(view).openFormActivity()
    }
}
