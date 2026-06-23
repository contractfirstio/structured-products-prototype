package com.contactfirstio.structuredproducts.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DisplayFormatters {
    private val dateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())

    fun enumLabel(value: Enum<*>): String =
        value.name.lowercase().replaceFirstChar { it.uppercase() }

    fun snakeCaseLabel(value: String): String =
        value.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

    fun formatInstant(instant: Instant): String = dateTimeFormatter.format(instant)
}
