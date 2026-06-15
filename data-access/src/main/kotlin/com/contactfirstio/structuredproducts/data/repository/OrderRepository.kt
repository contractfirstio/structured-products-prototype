package com.contactfirstio.structuredproducts.data.repository

import com.contactfirstio.structuredproducts.data.document.OrderDocument
import org.springframework.data.mongodb.repository.MongoRepository

interface OrderRepository : MongoRepository<OrderDocument, String> {
    fun existsByProductId(productId: String): Boolean
}
