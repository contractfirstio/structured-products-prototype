package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.catalog.BooleanValueNormalizer
import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.catalog.CommonFieldCatalog
import com.contactfirstio.structuredproducts.catalog.StandardFieldCatalog
import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.FieldKind
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class FieldCatalogService(
    private val attachmentValidator: AttachmentValidator,
) {

    fun standardFields(): List<CatalogFieldDefinition> = StandardFieldCatalog.fields

    fun commonFields(): List<CatalogFieldDefinition> = CommonFieldCatalog.fields

    fun standardFieldsByCategory(): Map<String, List<CatalogFieldDefinition>> =
        standardFields().groupBy { it.category }

    fun commonFieldsByCategory(): Map<String, List<CatalogFieldDefinition>> =
        commonFields().groupBy { it.category }

    fun standardCategories(): List<String> = StandardFieldCatalog.categories

    fun commonCategories(): List<String> = CommonFieldCatalog.categories

    fun findByKey(key: String): CatalogFieldDefinition? =
        standardFields().firstOrNull { it.key == key }
            ?: commonFields().firstOrNull { it.key == key }

    fun suggestedDefaultsForNewTemplate(): Map<String, String> {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val defaults = mutableMapOf<String, String>()

        standardFields()
            .filter { it.defaultedInTemplateCreation }
            .forEach { field ->
                when (field.key) {
                    "product_status" -> defaults[field.key] = "Draft"
                    "booking_center" -> defaults[field.key] = "HK/SG"
                    "market_segment" -> defaults[field.key] = "Institutional"
                    "itm_settlement_type" -> defaults[field.key] = "Cash"
                    "effective_from" -> defaults[field.key] = today.format(dateFormatter)
                    "effective_to" -> defaults[field.key] = today.plusYears(10).format(dateFormatter)
                    "investment_amount" -> defaults[field.key] = "Notional"
                    else ->
                        if (field.dataType == FieldDataType.BOOLEAN && field.enumOptions.contains("No")) {
                            defaults[field.key] = "No"
                        }
                }
            }

        return defaults
    }

    fun validateTemplateForm(command: SaveProductTemplateCommand): TemplateFormValidation {
        val missing = mutableListOf<String>()

        if (command.name.isBlank()) {
            missing.add("Template name")
        }
        if (command.description.isBlank()) {
            missing.add("Description")
        }

        standardFields()
            .filter { it.requiredInTemplateCreation }
            .forEach { field ->
                if (command.standardFieldDefaults[field.key].isNullOrBlank()) {
                    missing.add(field.displayName)
                }
            }

        return TemplateFormValidation(missingFieldLabels = missing)
    }

    fun validateDefaultValue(field: CatalogFieldDefinition, rawValue: String) {
        if (rawValue.isBlank()) return

        when (field.dataType) {
            FieldDataType.DOUBLE ->
                rawValue.toDoubleOrNull()
                    ?: throw ValidationException("Invalid number for ${field.displayName}")

            FieldDataType.BOOLEAN -> {
                if (!BooleanValueNormalizer.isValid(rawValue, field.enumOptions)) {
                    throw ValidationException("Invalid value for ${field.displayName}")
                }
            }

            FieldDataType.STRING, FieldDataType.DATE, FieldDataType.DATETIME -> Unit
            FieldDataType.FILE_LIST -> attachmentValidator.validateAttachmentIds(rawValue)
            FieldDataType.CHECKBOX_GROUP -> validateCheckboxGroupValue(field, rawValue)
        }

        if (field.enumOptions.isNotEmpty() &&
            field.dataType != FieldDataType.BOOLEAN &&
            field.dataType != FieldDataType.CHECKBOX_GROUP
        ) {
            if (rawValue !in field.enumOptions) {
                throw ValidationException("Invalid option for ${field.displayName}")
            }
        }
    }

    fun validateTemplateDefaults(defaults: Map<String, String>) {
        defaults.forEach { (key, value) ->
            if (value.isBlank()) return@forEach
            val field =
                findByKey(key)
                    ?: throw ValidationException("Unknown field key: $key")
            if (field.systemManagedAtProductCreation) {
                throw ValidationException("${field.displayName} is set by the system")
            }
            if (field.kind != FieldKind.STANDARD) {
                throw ValidationException("Only standard fields support fixed values")
            }
            if (!field.defaultedInTemplateCreation) {
                throw ValidationException("${field.displayName} does not support fixed values")
            }
            validateDefaultValue(field, value)
        }
    }

    fun validateRequiredTemplateDefaults(defaults: Map<String, String>) {
        standardFields()
            .filter { it.requiredInTemplateCreation }
            .forEach { field ->
                if (defaults[field.key].isNullOrBlank()) {
                    throw ValidationException("${field.displayName} is required")
                }
            }
    }

    fun validateIncludedCommonKeys(keys: Set<String>) {
        val knownKeys = commonFields().map { it.key }.toSet()
        val unknown = keys - knownKeys
        if (unknown.isNotEmpty()) {
            throw ValidationException("Unknown common field keys: ${unknown.joinToString()}")
        }
    }

    fun validateMandatoryCommonKeys(mandatoryKeys: Set<String>, includedKeys: Set<String>) {
        validateIncludedCommonKeys(mandatoryKeys)
        val notIncluded = mandatoryKeys - includedKeys
        if (notIncluded.isNotEmpty()) {
            throw ValidationException(
                "Mandatory common fields must be included: ${notIncluded.joinToString()}",
            )
        }
    }

    fun parseCheckboxGroupValue(rawValue: String?): Set<String> =
        rawValue
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            .orEmpty()

    fun serializeCheckboxGroupValue(selected: Set<String>): String? =
        selected.map { it.trim() }.filter { it.isNotEmpty() }.sorted().joinToString(",").ifBlank { null }

    private fun validateCheckboxGroupValue(field: CatalogFieldDefinition, rawValue: String) {
        val selected = parseCheckboxGroupValue(rawValue)
        if (selected.isEmpty()) return

        val invalid = selected.filter { it !in field.enumOptions }
        if (invalid.isNotEmpty()) {
            throw ValidationException("Invalid option for ${field.displayName}: ${invalid.joinToString()}")
        }
    }
}
