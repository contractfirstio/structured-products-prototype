package com.contactfirstio.structuredproducts.data.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "products")
data class ProductDocument(
    @Id val id: String? = null,
    val typeId: String,
    val status: ProductStatus,
    val underlying: String,
    val maturityMonths: Int,
    val legs: List<Map<String, Any>>,
)
