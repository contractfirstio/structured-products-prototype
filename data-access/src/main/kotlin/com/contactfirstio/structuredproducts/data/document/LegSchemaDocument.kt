package com.contactfirstio.structuredproducts.data.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "leg_schemas")
data class LegSchemaDocument(
    @Id val id: String? = null,
    val name: String,
    val fields: List<FieldDefinition>,
)
