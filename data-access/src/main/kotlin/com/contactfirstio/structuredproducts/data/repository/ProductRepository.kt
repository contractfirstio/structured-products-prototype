package com.contactfirstio.structuredproducts.data.repository

import com.contactfirstio.structuredproducts.data.document.ProductDocument
import com.contactfirstio.structuredproducts.data.document.ProductStatus
import org.springframework.data.mongodb.repository.MongoRepository

interface ProductRepository : MongoRepository<ProductDocument, String> {
    fun findByStatus(status: ProductStatus): List<ProductDocument>

    fun existsByTypeId(typeId: String): Boolean
}
