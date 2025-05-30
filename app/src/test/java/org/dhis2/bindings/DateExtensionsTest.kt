package org.dhis2.bindings

import android.content.Context
import org.dhis2.R
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.date.toOverdueOrScheduledUiText
import org.dhis2.commons.date.toUiText
import org.dhis2.commons.resources.ResourceManager
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DateExtensionsTest {

    val context: Context = mock()
    private val resourceManager: ResourceManager = mock()
    private val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
    private val uiFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    @Test
    fun `Should return empty when date is null`() {
        val date = null
        assert(date.toDateSpan(context) == "")
        assert(date.toUiText(context) == "")
    }

    @Test
    fun `Should return date format when date is after today`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time
        assert(date.toDateSpan(context, currentDate) == dateFormat.format(date))
        assert(date.toUiText(context, currentDate) == dateFormat.format(date))
    }

    @Test
    fun `Should return now when date is less than a minute from current date`() {
        val date = Date()
        whenever(context.getString(R.string.interval_now)) doReturn "now"
        assert(date.toDateSpan(context) == "now")
    }

    @Test
    fun `Should return 5 minutes ago when date is 5 minutes ago from current date`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.MINUTE, -5)
        }.time
        whenever(context.getString(R.string.interval_minute_ago)) doReturn "%d min. ago"
        assert(date.toDateSpan(context, currentDate) == "5 min. ago")
    }

    @Test
    fun `Should return 3 hours ago when date is 3 hours ago from current date`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.HOUR, -3)
        }.time
        whenever(context.getString(R.string.interval_hour_ago)) doReturn "%d hours ago"
        assert(date.toDateSpan(context, currentDate) == "3 hours ago")
    }

    @Test
    fun `Should return yesterday when date is more than 24h from current date`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time
        whenever(context.getString(R.string.interval_yesterday)) doReturn "Yesterday"
        whenever(context.getString(R.string.filter_period_yesterday)) doReturn "Yesterday"
        assert(date.toDateSpan(context, currentDate) == "Yesterday")
        assert(date.toUiText(context, currentDate) == "Yesterday")
    }

    @Test
    fun `Should return date format when date is more than 48h from current date`() {
        val date = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, -3)
        }.time
        assert(date.toDateSpan(context) == dateFormat.format(date))
    }

    @Test
    fun `Should return today when date is less than 24h from current date`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.HOUR, -20)
        }.time
        whenever(context.getString(R.string.filter_period_today)) doReturn "Today"
        assert(date.toUiText(context, currentDate) == "Today")
    }

    @Test
    fun `Should return dd MMM format when date is same year of current date`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.MONTH, -2)
        }.time
        assert(date.toUiText(context, currentDate) == uiFormat.format(date))
    }

    @Test
    fun `Should return date format format when date is more than one year from current date`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.YEAR, -2)
        }.time
        assert(date.toUiText(context, currentDate) == dateFormat.format(date))
    }

    @Test
    fun `Should return 'Today', when the overdue date is current date`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.HOUR, -20)
        }.time
        whenever(resourceManager.getString(R.string.overdue_today)) doReturn "Today"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "Today")
    }

    @Test
    fun `Should return '1 day overdue', when the overdue date is -1 day from the current date`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time
        whenever(resourceManager.getPlural(R.plurals.overdue_days, 1, 1)) doReturn "1 day overdue"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "1 day overdue")
    }

    @Test
    fun `Should return 'x days overdue', when the overdue date is -x days from the current date and less than 90 days`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, -15)
        }.time
        whenever(resourceManager.getPlural(R.plurals.overdue_days, 15, 15)) doReturn "15 days overdue"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "15 days overdue")
    }

    @Test
    fun `Should return 'x months overdue', when the overdue date is more than 3 months and less than 1 year`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.MONTH, -4)
        }.time
        whenever(resourceManager.getPlural(R.plurals.overdue_months, 4, 4)) doReturn "4 months overdue"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "4 months overdue")
    }

    @Test
    fun `Should return '1 year overdue', when the overdue date is 1 year`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.YEAR, -1)
        }.time
        whenever(resourceManager.getPlural(R.plurals.overdue_years, 1, 1)) doReturn "1 year overdue"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "1 year overdue")
    }

    @Test
    fun `Should return 'x years overdue', when the overdue date is more than 1 year`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.YEAR, -3)
        }.time
        whenever(resourceManager.getPlural(R.plurals.overdue_years, 3, 3)) doReturn "3 years overdue"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "3 years overdue")
    }

    @Test
    fun `Should return 'In 1 day', when the scheduled date is 1 day`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time
        whenever(resourceManager.getPlural(R.plurals.schedule_days, 1, 1)) doReturn "In 1 days"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "In 1 days")
    }

    @Test
    fun `Should return 'In 1 day', when the scheduled date is -x hours from current`() {
        val currentDate = currentCalendar().apply {
            set(Calendar.HOUR_OF_DAY, -2)
        }.time
        val date = currentCalendar().time
        whenever(resourceManager.getPlural(R.plurals.schedule_days, 1, 1)) doReturn "In 1 days"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "In 1 days")
    }

    @Test
    fun `Should return 'Today', when the scheduled date is same day with +x hours`() {
        val currentDate = currentCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 2)
        }.time
        val date = currentCalendar().time
        whenever(resourceManager.getString(R.string.overdue_today)) doReturn "Today"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "Today")
    }

    @Test
    fun `Should return 'In x days', when the scheduled date is same day but different month and same year`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.MONTH, 2)
        }.time
        whenever(resourceManager.getPlural(R.plurals.schedule_days, 61, 61)) doReturn "In 61 days"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "In 61 days")
    }

    @Test
    fun `Should return 'In x days', when the current date is -x days from the scheduled date and less than 90 days`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, 15)
        }.time
        whenever(resourceManager.getPlural(R.plurals.schedule_days, 15, 15)) doReturn "In 15 days"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "In 15 days")
    }

    @Test
    fun `Should return 'In x months', when the current date is more than 3 months and less than 1 year from schedule`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.MONTH, 4)
        }.time
        whenever(resourceManager.getPlural(R.plurals.schedule_months, 4, 4)) doReturn "In 4 months"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "In 4 months")
    }

    @Test
    fun `Should return 'In 1 year', when the scheduled date is in 1 year`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.YEAR, 1)
        }.time
        whenever(resourceManager.getPlural(R.plurals.schedule_years, 1, 1)) doReturn "In 1 year"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "In 1 year")
    }

    @Test
    fun `Should return 'In x years', when the scheduled date is in more than 1 year`() {
        val currentDate = currentCalendar().time
        val date = currentCalendar().apply {
            add(Calendar.YEAR, 3)
        }.time
        whenever(resourceManager.getPlural(R.plurals.schedule_years, 3, 3)) doReturn "In 3 years"
        assert(date.toOverdueOrScheduledUiText(resourceManager, currentDate) == "In 3 years")
    }

    private fun currentCalendar(): Calendar {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            time = "2020-03-02T00:00:00.00Z".toDate()
        }
    }
}
