package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.GlobalTermsSchema
import com.contactfirstio.structuredproducts.data.document.LegSchema
import com.contactfirstio.structuredproducts.data.document.ProductTypeDocument
import com.contactfirstio.structuredproducts.data.repository.ProductTypeRepository
import com.contactfirstio.structuredproducts.dsl.LegProcessorCatalog
import org.springframework.stereotype.Service

@Service
class ProductTypeService(
    private val productTypeRepository: ProductTypeRepository,
) {

    fun findAll(): List<ProductTypeDetail> =
        productTypeRepository.findAll().map { it.toDetail() }

    fun findById(id: String): ProductTypeDetail? =
        productTypeRepository.findById(id).orElse(null)?.toDetail()

    fun availableLegProcessorOptions(): List<LegProcessorOption> =
        LegProcessorCatalog.definitions.map {
            LegProcessorOption(
                schemaLegType = it.schemaLegType,
                displayName = it.displayName,
            )
        }

    fun save(command: CreateProductTypeCommand): ProductTypeDetail {
        if (command.name.isBlank()) {
            throw ValidationException("Product type name is required")
        }
        if (command.legSchemas.isEmpty()) {
            throw ValidationException("At least one allowed leg is required")
        }

        val saved = productTypeRepository.save(
            ProductTypeDocument(
                name = command.name.trim(),
                globalTermsSchema = GlobalTermsSchema(
                    requiresUnderlying = command.globalTermsSchema.requiresUnderlying,
                    requiresMaturityDate = command.globalTermsSchema.requiresMaturityDate,
                ),
                legSchemas = command.legSchemas.map {
                    LegSchema(
                        legType = it.legType,
                        isRequired = it.isRequired,
                    )
                },
            ),
        )

        return saved.toDetail()
    }

    fun update(id: String, command: CreateProductTypeCommand): ProductTypeDetail {
        val existing = productTypeRepository.findById(id)
            .orElseThrow { ValidationException("Product type not found: $id") }

        if (command.name.isBlank()) {
            throw ValidationException("Product type name is required")
        }
        if (command.legSchemas.isEmpty()) {
            throw ValidationException("At least one allowed leg is required")
        }

        val saved = productTypeRepository.save(
            existing.copy(
                name = command.name.trim(),
                globalTermsSchema = GlobalTermsSchema(
                    requiresUnderlying = command.globalTermsSchema.requiresUnderlying,
                    requiresMaturityDate = command.globalTermsSchema.requiresMaturityDate,
                ),
                legSchemas = command.legSchemas.map {
                    LegSchema(
                        legType = it.legType,
                        isRequired = it.isRequired,
                    )
                },
            ),
        )

        return saved.toDetail()
    }

    private fun ProductTypeDocument.toDetail(): ProductTypeDetail =
        ProductTypeDetail(
            id = id!!,
            name = name,
            globalTermsSchema = GlobalTermsSchemaDto(
                requiresUnderlying = globalTermsSchema.requiresUnderlying,
                requiresMaturityDate = globalTermsSchema.requiresMaturityDate,
            ),
            legSchemas = legSchemas.map { leg ->
                val definition = LegProcessorCatalog.findBySchemaLegType(leg.legType)
                LegSchemaDto(
                    legType = leg.legType,
                    isRequired = leg.isRequired,
                    parameterLabel = definition?.parameterLabel ?: leg.legType,
                    processorLegType = definition?.processorLegType
                        ?: LegProcessorCatalog.resolveProcessorLegType(leg.legType),
                )
            },
        )
}
