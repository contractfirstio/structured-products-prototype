package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.repository.OrderRepository
import com.contactfirstio.structuredproducts.data.repository.ProductRepository
import org.springframework.stereotype.Component

@Component
class ReferentialIntegrityGuard(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val productTypeRepository: com.contactfirstio.structuredproducts.data.repository.ProductTypeRepository,
) {

    fun ensureLegSchemaIsUnreferenced(legSchemaId: String, action: String) {
        if (productTypeRepository.existsByAllowedLegSchemaIdsContaining(legSchemaId)) {
            throw ValidationException("Cannot $action leg schema while product types reference it")
        }
    }

    fun ensureProductTypeIsUnreferenced(typeId: String, action: String) {
        if (productRepository.existsByTypeId(typeId)) {
            throw ValidationException("Cannot $action product type while product instances reference it")
        }
    }

    fun ensureProductInstanceIsUnreferenced(productId: String, action: String) {
        if (orderRepository.existsByProductId(productId)) {
            throw ValidationException("Cannot $action product instance while orders reference it")
        }
    }
}
