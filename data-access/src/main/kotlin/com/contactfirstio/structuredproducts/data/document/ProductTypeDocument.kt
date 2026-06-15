package com.contactfirstio.structuredproducts.data.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "product_types")
data class ProductTypeDocument(
    @Id val id: String? = null,
    val name: String,
    val globalTermsSchema: GlobalTermsSchema,
    val legSchemas: List<LegSchema>,
)
