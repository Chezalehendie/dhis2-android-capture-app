package org.dhis2.usescases.enrollment

import org.dhis2.usescases.customConfigTransformation.AutoEnrollmentConfigurations

data class CustomConfigAndEvents(val enrollmentConfig: AutoEnrollmentConfigurations, val eventAndEnrollmentIds: Pair<String, String?> )
