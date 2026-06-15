package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.contactfirstio.structuredproducts.data.document.ProductTypeDocument
import org.springframework.stereotype.Component

@Component
class ProductStructureValidator(
    private val legSchemaService: LegSchemaService,
) {

    fun validate(type: ProductTypeDocument, command: CreateProductInstanceCommand) {
        val schema = type.globalTermsSchema

        if (schema.requiresUnderlying && command.underlying.isNullOrBlank()) {
            throw ValidationException("Underlying is required for product type '${type.name}'")
        }

        if (schema.requiresMaturityDate && command.maturityMonths == null) {
            throw ValidationException("Maturity is required for product type '${type.name}'")
        }

        val legSchemas = legSchemaService.findByIds(type.allowedLegSchemaIds)
        if (legSchemas.size != type.allowedLegSchemaIds.size) {
            throw ValidationException("Product type '${type.name}' references missing leg schemas")
        }

        if (command.legData.size != legSchemas.size) {
            throw ValidationException("Expected ${legSchemas.size} leg(s) for product type '${type.name}'")
        }

        legSchemas.zip(command.legData).forEach { (legSchema, legData) ->
            legSchema.fields.forEach { field ->
                if (field.isRequired && isBlankValue(legData[field.fieldName])) {
                    throw ValidationException(
                        "${field.fieldName} is required for leg '${legSchema.name}' on product type '${type.name}'",
                    )
                }
            }
        }
    }

    fun buildLegs(type: ProductTypeDocument, legData: List<Map<String, Any>>): List<Map<String, Any>> {
        val legSchemas = legSchemaService.findByIds(type.allowedLegSchemaIds)

        return legSchemas.zip(legData).map { (legSchema, data) ->
            val stored = linkedMapOf<String, Any>("legSchemaId" to legSchema.id)
            legSchema.fields.forEach { field ->
                data[field.fieldName]?.let { value ->
                    stored[field.fieldName] = normalizeValue(field.dataType, value)
                }
            }
            stored
        }
    }

    fun determineStatus(type: ProductTypeDocument, legData: List<Map<String, Any>>): ProductStatus {
        val legSchemas = legSchemaService.findByIds(type.allowedLegSchemaIds)

        val hasOptionalGap = legSchemas.zip(legData).any { (legSchema, data) ->
            legSchema.fields.any { field ->
                !field.isRequired && isBlankValue(data[field.fieldName])
            }
        }

        return if (hasOptionalGap) ProductStatus.DRAFT else ProductStatus.ACTIVE
    }

    private fun isBlankValue(value: Any?): Boolean =
        when (value) {
            null -> true
            is String -> value.isBlank()
            else -> false
        }

    private fun normalizeValue(dataType: FieldDataType, value: Any): Any =
        when (dataType) {
            FieldDataType.STRING -> value.toString()
            FieldDataType.DOUBLE -> (value as? Number)?.toDouble()
                ?: value.toString().toDouble()
            FieldDataType.INTEGER -> (value as? Number)?.toInt()
                ?: value.toString().toInt()
            FieldDataType.BOOLEAN -> when (value) {
                is Boolean -> value
                is String -> value.toBooleanStrictOrNull() ?: value.equals("true", ignoreCase = true)
                else -> value.toString().toBoolean()
            }
            FieldDataType.ENUM -> value.toString()
        }
}
