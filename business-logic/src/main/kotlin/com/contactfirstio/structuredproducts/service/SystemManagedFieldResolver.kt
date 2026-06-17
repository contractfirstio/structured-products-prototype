package com.contactfirstio.structuredproducts.service

import org.springframework.stereotype.Service

@Service
class SystemManagedFieldResolver {

    /**
     * Resolves standard field values that are owned by the platform rather than templates.
     * Product creator will be derived from the authenticated user once auth is implemented.
     */
    fun resolveAtProductCreation(fieldKey: String): String? =
        when (fieldKey) {
            PRODUCT_CREATOR_FIELD_KEY -> resolveProductCreator()
            else -> null
        }

    fun resolveProductCreator(): String? = null

    companion object {
        const val PRODUCT_CREATOR_FIELD_KEY = "product_creator"
    }
}
