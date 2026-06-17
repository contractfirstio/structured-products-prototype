package com.contactfirstio.structuredproducts.service

import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class SystemManagedFieldResolver {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC)

    /**
     * Resolves standard field values that are owned by the platform rather than templates.
     * Product creator will be derived from the authenticated user once auth is implemented.
     */
    fun resolveAtProductCreation(fieldKey: String): String? =
        when (fieldKey) {
            PRODUCT_CREATOR_FIELD_KEY -> resolveProductCreator()
            CREATION_TIMESTAMP_FIELD_KEY -> resolveCreationTimestamp()
            else -> null
        }

    fun resolveProductCreator(): String? = null

    fun resolveCreationTimestamp(): String = dateTimeFormatter.format(Instant.now())

    companion object {
        const val PRODUCT_CREATOR_FIELD_KEY = "product_creator"
        const val CREATION_TIMESTAMP_FIELD_KEY = "creation_timestamp"
    }
}
