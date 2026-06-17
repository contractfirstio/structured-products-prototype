package com.contactfirstio.structuredproducts.catalog

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.FieldKind
import com.contactfirstio.structuredproducts.data.document.FieldRequirement

data class CatalogFieldDefinition(
    val key: String,
    val category: String,
    val displayName: String,
    val dataType: FieldDataType,
    val requirement: FieldRequirement,
    val enumOptions: List<String> = emptyList(),
    val defaultedInTemplateCreation: Boolean = false,
    val excludedFromFixedValuesOnly: Boolean = false,
    val systemManagedAtProductCreation: Boolean = false,
    val kind: FieldKind,
)
