package com.contactfirstio.structuredproducts.data.document

data class FieldDefinition(
    val fieldName: String,
    val dataType: FieldDataType,
    val isRequired: Boolean,
    val enumOptions: List<String> = emptyList(),
)
