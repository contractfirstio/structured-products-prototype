package com.contactfirstio.structuredproducts.service

data class TemplateFormValidation(
    val missingFieldLabels: List<String> = emptyList(),
) {
    val isValid: Boolean get() = missingFieldLabels.isEmpty()

    val hasStandardFieldErrors: Boolean
        get() = missingFieldLabels.any { it !in METADATA_FIELD_LABELS }

    companion object {
        private val METADATA_FIELD_LABELS = setOf("Template name", "Description")
    }
}
