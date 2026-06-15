package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.GlobalTermsSchema
import com.contactfirstio.structuredproducts.data.document.LegSchema
import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.contactfirstio.structuredproducts.data.document.ProductTypeDocument
import com.contactfirstio.structuredproducts.dsl.LegProcessorCatalog
import org.springframework.stereotype.Component

@Component
class ProductStructureValidator {

    fun validate(type: ProductTypeDocument, command: CreateProductInstanceCommand) {
        val schema = type.globalTermsSchema

        if (schema.requiresUnderlying && command.underlying.isNullOrBlank()) {
            throw ValidationException("Underlying is required for product type '${type.name}'")
        }

        if (schema.requiresMaturityDate && command.maturityMonths == null) {
            throw ValidationException("Maturity is required for product type '${type.name}'")
        }

        type.legSchemas.forEach { legSchema ->
            val processorLegType = LegProcessorCatalog.resolveProcessorLegType(legSchema.legType)
            val value = command.legValues[processorLegType]
            if (legSchema.isRequired && value == null) {
                val label = LegProcessorCatalog.findBySchemaLegType(legSchema.legType)?.parameterLabel
                    ?: legSchema.legType
                throw ValidationException("$label is required for product type '${type.name}'")
            }
        }
    }

    fun buildLegs(type: ProductTypeDocument, legValues: Map<String, Double?>): List<Map<String, Any>> {
        val legs = mutableListOf<Map<String, Any>>()

        type.legSchemas.forEach { legSchema ->
            val definition = LegProcessorCatalog.findBySchemaLegType(legSchema.legType)
                ?: throw ValidationException("Unknown leg type '${legSchema.legType}' on product type '${type.name}'")
            val value = legValues[definition.processorLegType] ?: return@forEach

            legs.add(
                mapOf(
                    "type" to definition.processorLegType,
                    definition.parameterKey to value,
                ),
            )
        }

        if (legs.isEmpty()) {
            throw ValidationException("At least one leg value is required")
        }

        return legs
    }

    fun determineStatus(type: ProductTypeDocument, legValues: Map<String, Double?>): ProductStatus {
        val upsideDefinition = LegProcessorCatalog.findByProcessorLegType("upside")
        if (upsideDefinition != null && type.legSchemas.any { it.legType == upsideDefinition.schemaLegType }) {
            val participation = legValues["upside"]
            return if (participation != null) {
                ProductStatus.ACTIVE
            } else {
                ProductStatus.DRAFT
            }
        }

        return ProductStatus.ACTIVE
    }
}
