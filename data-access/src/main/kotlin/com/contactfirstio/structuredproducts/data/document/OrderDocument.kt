package com.contactfirstio.structuredproducts.data.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "orders")
data class OrderDocument(
    @Id val id: String? = null,
    val productId: String,
    val notionalInvested: Double,
    val status: OrderStatus,
)
