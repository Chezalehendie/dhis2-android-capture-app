package org.dhis2.usescases.enrollment
import org.dhis2.usescases.customConfigTransformation.AutoEnrollmentConfigurations

/**
 * Data class that encapsulates both the auto-enrollment configuration and
 * the resulting IDs of the created enrollment and event.
 *
 * This is returned after the process of applying cross-program
 * mapping rules and generating new events/enrollments during TEI registration.
 *
 * @property enrollmentConfig The full configuration containing mapping rules and transformation logic.
 * @property eventAndEnrollmentIds A pair containing the enrollment ID (first) and optionally the event ID (second).
 */

data class CustomConfigAndEvents(
    val enrollmentConfig: AutoEnrollmentConfigurations,
    val eventAndEnrollmentIds: Pair<String, String?>
)
