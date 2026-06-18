package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.ProductTemplateDocument
import com.contactfirstio.structuredproducts.data.document.TemplateCustomFieldDefinition
import com.contactfirstio.structuredproducts.data.document.TemplateCustomFieldType
import com.contactfirstio.structuredproducts.data.repository.ProductTemplateRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ProductTemplateService(
    private val productTemplateRepository: ProductTemplateRepository,
    private val fieldCatalogService: FieldCatalogService,
    private val templateAttachmentService: TemplateAttachmentService,
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
                mandatoryCommonFieldKeys = sanitizeMandatoryCommonKeys(command),
                caCaaDeclarationQuestions = sanitizeCaCaaQuestions(command.caCaaDeclarationQuestions),
                customFields = sanitizeCustomFields(command.customFields),
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
                mandatoryCommonFieldKeys = sanitizeMandatoryCommonKeys(command),
                caCaaDeclarationQuestions = sanitizeCaCaaQuestions(command.caCaaDeclarationQuestions),
                customFields = sanitizeCustomFields(command.customFields),
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
        if (command.description.isBlank()) {
            throw ValidationException("Description is required")
        }
        fieldCatalogService.validateRequiredTemplateDefaults(command.standardFieldDefaults)
        fieldCatalogService.validateTemplateDefaults(command.standardFieldDefaults)
        fieldCatalogService.validateIncludedCommonKeys(command.includedCommonFieldKeys)
        fieldCatalogService.validateMandatoryCommonKeys(
            command.mandatoryCommonFieldKeys,
            command.includedCommonFieldKeys,
        )
        validateCustomFields(sanitizeCustomFields(command.customFields))
    }

    private fun sanitizeMandatoryCommonKeys(command: SaveProductTemplateCommand): List<String> =
        command.mandatoryCommonFieldKeys
            .intersect(command.includedCommonFieldKeys)
            .toList()
            .sorted()

    private fun sanitizeDefaults(defaults: Map<String, String>): Map<String, String> =
        defaults
            .filterKeys { key ->
                fieldCatalogService.findByKey(key)?.systemManagedAtProductCreation != true
            }
            .filterValues { it.isNotBlank() }
            .mapValues { (_, value) -> value.trim() }

    private fun sanitizeCaCaaQuestions(questions: List<String>): List<String> =
        questions.map { it.trim() }.filter { it.isNotEmpty() }

    private fun sanitizeCustomFields(fields: List<TemplateCustomFieldDefinition>): List<TemplateCustomFieldDefinition> {
        val usedKeys = mutableSetOf<String>()
        return fields
            .map { field ->
                field.copy(
                    label = field.label.trim(),
                    enumOptions = field.enumOptions.map { it.trim() }.filter { it.isNotEmpty() },
                )
            }
            .filter { it.label.isNotEmpty() }
            .map { field ->
                val baseKey = field.key.trim().ifBlank { slugifyKey(field.label) }.ifBlank { "field" }
                var uniqueKey = baseKey
                var suffix = 2
                while (uniqueKey in usedKeys) {
                    uniqueKey = "${baseKey}_$suffix"
                    suffix++
                }
                usedKeys.add(uniqueKey)
                field.copy(key = uniqueKey)
            }
    }

    private fun validateCustomFields(fields: List<TemplateCustomFieldDefinition>) {
        fields.forEach { field ->
            if (field.dataType == TemplateCustomFieldType.ENUM && field.enumOptions.isEmpty()) {
                throw ValidationException("Enum field \"${field.label}\" must have at least one option")
            }
            fieldCatalogService.findByKey(field.key)?.let { catalogField ->
                throw ValidationException(
                    "Custom field key \"${field.key}\" conflicts with catalog field \"${catalogField.displayName}\"",
                )
            }
        }
    }

    private fun slugifyKey(label: String): String =
        label
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

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
            caCaaDeclarationQuestionCount = caCaaDeclarationQuestions.size,
            customFieldCount = customFields.size,
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
            mandatoryCommonFieldKeys = mandatoryCommonFieldKeys.toSet(),
            caCaaDeclarationQuestions = caCaaDeclarationQuestions,
            customFields = sanitizeCustomFields(customFields),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
