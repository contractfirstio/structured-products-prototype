package com.contactfirstio.structuredproducts.catalog

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.FieldKind
import com.contactfirstio.structuredproducts.data.document.FieldRequirement

object CatalogFieldBuilder {
    fun field(
        kind: FieldKind,
        key: String,
        category: String,
        displayName: String,
        dataType: FieldDataType,
        requirement: FieldRequirement,
        enumOptions: List<String> = emptyList(),
        defaultedInTemplateCreation: Boolean = false,
        requiredInTemplateCreation: Boolean = false,
        excludedFromFixedValuesOnly: Boolean = false,
        systemManagedAtProductCreation: Boolean = false,
    ): CatalogFieldDefinition =
        CatalogFieldDefinition(
            key = key,
            category = category,
            displayName = displayName,
            dataType = dataType,
            requirement = requirement,
            enumOptions = enumOptions,
            defaultedInTemplateCreation = defaultedInTemplateCreation,
            requiredInTemplateCreation = requiredInTemplateCreation,
            excludedFromFixedValuesOnly = excludedFromFixedValuesOnly,
            systemManagedAtProductCreation = systemManagedAtProductCreation,
            kind = kind,
        )
}

object BooleanValueNormalizer {
    fun normalize(rawValue: String, enumOptions: List<String>): String {
        if (enumOptions.isNotEmpty()) {
            return enumOptions.firstOrNull { it.equals(rawValue, ignoreCase = true) } ?: rawValue
        }
        return when (rawValue.lowercase()) {
            "true" -> "true"
            "false" -> "false"
            else -> rawValue
        }
    }

    fun isValid(rawValue: String, enumOptions: List<String>): Boolean {
        if (enumOptions.isNotEmpty()) {
            return enumOptions.any { it.equals(rawValue, ignoreCase = true) }
        }
        return rawValue.lowercase() in setOf("true", "false")
    }
}
