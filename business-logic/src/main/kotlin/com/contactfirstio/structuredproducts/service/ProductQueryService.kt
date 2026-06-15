package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.contactfirstio.structuredproducts.data.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductQueryService(
    private val productRepository: ProductRepository,
) {

    fun findActiveProducts(): List<ActiveProductSummary> =
        productRepository.findByStatus(ProductStatus.ACTIVE).map {
            ActiveProductSummary(
                id = it.id!!,
                underlying = it.underlying,
                maturityMonths = it.maturityMonths,
            )
        }
}
