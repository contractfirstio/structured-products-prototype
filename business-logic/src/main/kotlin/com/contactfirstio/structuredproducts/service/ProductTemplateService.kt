package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.ProductTemplateDocument
import com.contactfirstio.structuredproducts.data.repository.ProductTemplateRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ProductTemplateService(
    private val productTemplateRepository: ProductTemplateRepository,
    private val fieldCatalogService: FieldCatalogService,
    private val templateAttachmentService: TemplateAttachmentService,
    private val caCaaDeclarationCatalogService: CaCaaDeclarationCatalogService,
) {

    fun findAll(): List<ProductTemplateSummary> =
        productTemplateRepository.findAll()
            .sortedByDescending { it.updatedAt }
            .map { it.toSummary() }

    fun findById(id: String): ProductTemplateDetail? =
        productTemplateRepository.findById(id).orElse(null)?.toDetail()

    fun create(command: SaveProductTemplateCommand): ProductTemplateDetail {
        validate(command)
        val now = Instant.now()
        val document =
            ProductTemplateDocument(
                name = command.name.trim(),
                description = command.description.trim(),
                status = command.status,
                standardFieldDefaults = sanitizeDefaults(command.standardFieldDefaults),
                includedCommonFieldKeys = command.includedCommonFieldKeys.toList().sorted(),
                includedCaCaaDeclarationQuestionKeys =
                    command.includedCaCaaDeclarationQuestionKeys.toList().sorted(),
                createdAt = now,
                updatedAt = now,
            )
        return productTemplateRepository.save(document).toDetail().also {
            syncTemplateAttachments(it.id, it.standardFieldDefaults)
        }
    }

    fun update(id: String, command: SaveProductTemplateCommand): ProductTemplateDetail {
        validate(command)
        val existing =
            productTemplateRepository.findById(id).orElse(null)
                ?: throw ValidationException("Product template not found: $id")

        val updated =
            existing.copy(
                name = command.name.trim(),
                description = command.description.trim(),
                status = command.status,
                standardFieldDefaults = sanitizeDefaults(command.standardFieldDefaults),
                includedCommonFieldKeys = command.includedCommonFieldKeys.toList().sorted(),
                includedCaCaaDeclarationQuestionKeys =
                    command.includedCaCaaDeclarationQuestionKeys.toList().sorted(),
                updatedAt = Instant.now(),
            )
        return productTemplateRepository.save(updated).toDetail().also {
            syncTemplateAttachments(it.id, it.standardFieldDefaults)
        }
    }

    fun delete(id: String) {
        if (!productTemplateRepository.existsById(id)) {
            throw ValidationException("Product template not found: $id")
        }
        templateAttachmentService.deleteAllForTemplate(id)
        productTemplateRepository.deleteById(id)
    }

    private fun validate(command: SaveProductTemplateCommand) {
        if (command.name.isBlank()) {
            throw ValidationException("Template name is required")
        }
        fieldCatalogService.validateTemplateDefaults(command.standardFieldDefaults)
        fieldCatalogService.validateIncludedCommonKeys(command.includedCommonFieldKeys)
        caCaaDeclarationCatalogService.validateIncludedQuestionKeys(command.includedCaCaaDeclarationQuestionKeys)
    }

    private fun sanitizeDefaults(defaults: Map<String, String>): Map<String, String> =
        defaults
            .filterKeys { key ->
                fieldCatalogService.findByKey(key)?.systemManagedAtProductCreation != true
            }
            .filterValues { it.isNotBlank() }
            .mapValues { (_, value) -> value.trim() }

    private fun syncTemplateAttachments(templateId: String, defaults: Map<String, String>) {
        defaults.forEach { (key, value) ->
            val field = fieldCatalogService.findByKey(key) ?: return@forEach
            if (field.dataType != FieldDataType.FILE_LIST) return@forEach

            val attachmentIds = templateAttachmentService.parseAttachmentIds(value).toSet()
            templateAttachmentService.linkToTemplate(attachmentIds, templateId)
            templateAttachmentService.deleteUnreferencedForTemplate(templateId, key, attachmentIds)
        }
    }

    private fun ProductTemplateDocument.toSummary(): ProductTemplateSummary =
        ProductTemplateSummary(
            id = id!!,
            name = name,
            description = description,
            status = status,
            standardFieldCount = fieldCatalogService.standardFields().size,
            commonFieldsSelected = includedCommonFieldKeys.size,
            commonFieldsTotal = fieldCatalogService.commonFields().size,
            caCaaDeclarationQuestionsSelected = includedCaCaaDeclarationQuestionKeys.size,
            caCaaDeclarationQuestionsTotal = caCaaDeclarationCatalogService.questions().size,
            updatedAt = updatedAt,
        )

    private fun ProductTemplateDocument.toDetail(): ProductTemplateDetail =
        ProductTemplateDetail(
            id = id!!,
            name = name,
            description = description,
            status = status,
            standardFieldDefaults = sanitizeDefaults(standardFieldDefaults),
            includedCommonFieldKeys = includedCommonFieldKeys.toSet(),
            includedCaCaaDeclarationQuestionKeys = includedCaCaaDeclarationQuestionKeys.toSet(),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
