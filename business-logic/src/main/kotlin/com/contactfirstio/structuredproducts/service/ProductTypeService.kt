package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.GlobalTermsSchema
import com.contactfirstio.structuredproducts.data.document.ProductTypeDocument
import com.contactfirstio.structuredproducts.data.repository.ProductTypeRepository
import org.springframework.stereotype.Service

@Service
class ProductTypeService(
    private val productTypeRepository: ProductTypeRepository,
    private val legSchemaService: LegSchemaService,
) {

    fun findAll(): List<ProductTypeDetail> =
        productTypeRepository.findAll().map { it.toDetail() }

    fun findById(id: String): ProductTypeDetail? =
        productTypeRepository.findById(id).orElse(null)?.toDetail()

    fun save(command: CreateProductTypeCommand): ProductTypeDetail {
        if (command.name.isBlank()) {
            throw ValidationException("Product type name is required")
        }
        if (command.allowedLegSchemaIds.isEmpty()) {
            throw ValidationException("At least one allowed leg schema is required")
        }

        validateLegSchemaIds(command.allowedLegSchemaIds)

        val saved = productTypeRepository.save(
            ProductTypeDocument(
                name = command.name.trim(),
                globalTermsSchema = GlobalTermsSchema(
                    requiresUnderlying = command.globalTermsSchema.requiresUnderlying,
                    requiresMaturityDate = command.globalTermsSchema.requiresMaturityDate,
                ),
                allowedLegSchemaIds = command.allowedLegSchemaIds,
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
        if (command.allowedLegSchemaIds.isEmpty()) {
            throw ValidationException("At least one allowed leg schema is required")
        }

        validateLegSchemaIds(command.allowedLegSchemaIds)

        val saved = productTypeRepository.save(
            existing.copy(
                name = command.name.trim(),
                globalTermsSchema = GlobalTermsSchema(
                    requiresUnderlying = command.globalTermsSchema.requiresUnderlying,
                    requiresMaturityDate = command.globalTermsSchema.requiresMaturityDate,
                ),
                allowedLegSchemaIds = command.allowedLegSchemaIds,
            ),
        )

        return saved.toDetail()
    }

    private fun validateLegSchemaIds(ids: List<String>) {
        val resolved = legSchemaService.findByIds(ids)
        if (resolved.size != ids.size) {
            throw ValidationException("One or more leg schemas were not found")
        }
    }

    private fun ProductTypeDocument.toDetail(): ProductTypeDetail {
        val legSchemas = legSchemaService.findByIds(allowedLegSchemaIds)
        return ProductTypeDetail(
            id = id!!,
            name = name,
            globalTermsSchema = GlobalTermsSchemaDto(
                requiresUnderlying = globalTermsSchema.requiresUnderlying,
                requiresMaturityDate = globalTermsSchema.requiresMaturityDate,
            ),
            allowedLegSchemaIds = allowedLegSchemaIds,
            legSchemas = legSchemas,
        )
    }
}
