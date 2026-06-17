package com.contactfirstio.structuredproducts.data.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "product_templates")
data class ProductTemplateDocument(
    @Id val id: String? = null,
    val name: String,
    val description: String = "",
    val status: TemplateStatus = TemplateStatus.DRAFT,
    val standardFieldDefaults: Map<String, String> = emptyMap(),
    val includedCommonFieldKeys: List<String> = emptyList(),
    val mandatoryCommonFieldKeys: List<String> = emptyList(),
    val caCaaDeclarationQuestions: List<String> = emptyList(),
    val customFields: List<TemplateCustomFieldDefinition> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
)
