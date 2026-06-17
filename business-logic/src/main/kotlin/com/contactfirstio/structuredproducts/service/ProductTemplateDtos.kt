package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.TemplateCustomFieldDefinition
import com.contactfirstio.structuredproducts.data.document.TemplateStatus
import java.time.Instant

data class ProductTemplateSummary(
    val id: String,
    val name: String,
    val description: String,
    val status: TemplateStatus,
    val standardFieldCount: Int,
    val commonFieldsSelected: Int,
    val commonFieldsTotal: Int,
    val caCaaDeclarationQuestionCount: Int,
    val customFieldCount: Int,
    val updatedAt: Instant,
)

data class ProductTemplateDetail(
    val id: String,
    val name: String,
    val description: String,
    val status: TemplateStatus,
    val standardFieldDefaults: Map<String, String>,
    val includedCommonFieldKeys: Set<String>,
    val mandatoryCommonFieldKeys: Set<String>,
    val caCaaDeclarationQuestions: List<String>,
    val customFields: List<TemplateCustomFieldDefinition>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class SaveProductTemplateCommand(
    val name: String,
    val description: String = "",
    val status: TemplateStatus = TemplateStatus.DRAFT,
    val standardFieldDefaults: Map<String, String> = emptyMap(),
    val includedCommonFieldKeys: Set<String> = emptySet(),
    val mandatoryCommonFieldKeys: Set<String> = emptySet(),
    val caCaaDeclarationQuestions: List<String> = emptyList(),
    val customFields: List<TemplateCustomFieldDefinition> = emptyList(),
)
