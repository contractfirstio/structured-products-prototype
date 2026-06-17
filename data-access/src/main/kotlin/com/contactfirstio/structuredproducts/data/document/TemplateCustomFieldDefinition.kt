package com.contactfirstio.structuredproducts.data.document

data class TemplateCustomFieldDefinition(
    val key: String = "",
    val label: String,
    val dataType: TemplateCustomFieldType,
    val enumOptions: List<String> = emptyList(),
    val mandatory: Boolean = false,
)
