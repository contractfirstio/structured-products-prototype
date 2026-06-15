package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.ProductDocument
import com.contactfirstio.structuredproducts.data.repository.ProductRepository
import com.contactfirstio.structuredproducts.data.repository.ProductTypeRepository
import org.springframework.stereotype.Service

@Service
class ProductInstanceService(
    private val productRepository: ProductRepository,
    private val productTypeRepository: ProductTypeRepository,
    private val productTypeService: ProductTypeService,
    private val productStructureValidator: ProductStructureValidator,
) {

    fun saveProduct(command: CreateProductInstanceCommand): SavedProductResult {
        val productType = productTypeRepository.findById(command.typeId)
            .orElseThrow { ValidationException("Product type not found: ${command.typeId}") }

        return saveValidatedProduct(productType, command, null)
    }

    fun getForEdit(id: String): ProductInstanceEditDetail {
        val product = productRepository.findById(id)
            .orElseThrow { ValidationException("Product instance not found: $id") }
        val productType = productTypeService.findById(product.typeId)
            ?: throw ValidationException("Product type not found: ${product.typeId}")

        return ProductInstanceEditDetail(
            id = product.id!!,
            typeId = product.typeId,
            productType = productType,
            underlying = product.underlying.takeIf { it.isNotBlank() },
            maturityMonths = product.maturityMonths.takeIf { it > 0 },
            legData = parseLegData(product.legs),
        )
    }

    fun updateProduct(id: String, command: CreateProductInstanceCommand): SavedProductResult {
        val existing = productRepository.findById(id)
            .orElseThrow { ValidationException("Product instance not found: $id") }

        if (command.typeId != existing.typeId) {
            throw ValidationException("Product type cannot be changed after creation")
        }

        val productType = productTypeRepository.findById(existing.typeId)
            .orElseThrow { ValidationException("Product type not found: ${existing.typeId}") }

        return saveValidatedProduct(productType, command, existing.id)
    }

    private fun saveValidatedProduct(
        productType: com.contactfirstio.structuredproducts.data.document.ProductTypeDocument,
        command: CreateProductInstanceCommand,
        existingId: String?,
    ): SavedProductResult {
        productStructureValidator.validate(productType, command)

        val legs = productStructureValidator.buildLegs(productType, command.legData)
        val status = productStructureValidator.determineStatus(productType, command.legData)

        val product = ProductDocument(
            id = existingId,
            typeId = command.typeId,
            status = status,
            underlying = command.underlying?.trim().orEmpty(),
            maturityMonths = command.maturityMonths ?: 0,
            legs = legs,
        )

        val saved = productRepository.save(product)
        return SavedProductResult(
            id = saved.id!!,
            status = saved.status.name,
            underlying = saved.underlying,
        )
    }

    private fun parseLegData(legs: List<Map<String, Any>>): List<Map<String, Any>> =
        legs.map { leg ->
            leg.filterKeys { it != "legSchemaId" }
        }
}
