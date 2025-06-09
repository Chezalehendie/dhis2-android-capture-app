package org.dhis2.usescases.enrollment.events

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity

@PerActivity
@Subcomponent(modules = [ScheduledEventModule::class])
interface ScheduledEventComponent {
    fun inject(scheduledEventActivity: ScheduledEventActivity)
}
