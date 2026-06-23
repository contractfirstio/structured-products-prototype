package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.TemplateAttachmentDocument
import com.contactfirstio.structuredproducts.data.repository.TemplateAttachmentRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TemplateAttachmentService(
    private val templateAttachmentRepository: TemplateAttachmentRepository,
) : AttachmentValidator {

    fun store(
        templateId: String?,
        fieldKey: String,
        fileName: String,
        contentType: String,
        content: ByteArray,
    ): TemplateAttachmentSummary {
        require(fileName.isNotBlank()) { "File name is required" }
        require(content.isNotEmpty()) { "File is empty" }
        if (content.size > MAX_FILE_SIZE_BYTES) {
            throw ValidationException("File exceeds maximum size of ${MAX_FILE_SIZE_BYTES / 1024 / 1024} MB")
        }

        val document =
            TemplateAttachmentDocument(
                templateId = templateId,
                fieldKey = fieldKey,
                fileName = fileName.trim(),
                contentType = contentType.ifBlank { "application/octet-stream" },
                size = content.size.toLong(),
                content = content,
                uploadedAt = Instant.now(),
            )
        val saved = templateAttachmentRepository.save(document)
        return saved.toSummary()
    }

    fun findById(id: String): TemplateAttachmentContent? =
        templateAttachmentRepository.findById(id).orElse(null)?.toContent()

    fun findSummaries(ids: Collection<String>): List<TemplateAttachmentSummary> {
        if (ids.isEmpty()) return emptyList()
        return templateAttachmentRepository.findAllById(ids).map { it.toSummary() }
    }

    fun delete(id: String) {
        if (templateAttachmentRepository.existsById(id)) {
            templateAttachmentRepository.deleteById(id)
        }
    }

    fun linkToTemplate(attachmentIds: Collection<String>, templateId: String) {
        if (attachmentIds.isEmpty()) return
        templateAttachmentRepository.findAllById(attachmentIds).forEach { attachment ->
            if (attachment.templateId != templateId) {
                templateAttachmentRepository.save(attachment.copy(templateId = templateId))
            }
        }
    }

    fun deleteUnreferencedForTemplate(
        templateId: String,
        fieldKey: String,
        referencedIds: Set<String>,
    ) {
        templateAttachmentRepository.findByTemplateId(templateId)
            .filter { it.fieldKey == fieldKey && it.id !in referencedIds }
            .forEach { templateAttachmentRepository.delete(it) }
    }

    fun deleteAllForTemplate(templateId: String) {
        templateAttachmentRepository.findByTemplateId(templateId)
            .forEach { templateAttachmentRepository.delete(it) }
    }

    fun parseAttachmentIds(rawValue: String?): List<String> =
        rawValue
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()

    fun serializeAttachmentIds(ids: Collection<String>): String? =
        ids.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(",").ifBlank { null }

    override fun validateAttachmentIds(rawValue: String) {
        val ids = parseAttachmentIds(rawValue)
        if (ids.isEmpty()) return

        val found = templateAttachmentRepository.findAllById(ids).map { it.id }.toSet()
        val missing = ids.filter { it !in found }
        if (missing.isNotEmpty()) {
            throw ValidationException("Unknown attachment ids: ${missing.joinToString()}")
        }
    }

    private fun TemplateAttachmentDocument.toSummary(): TemplateAttachmentSummary =
        TemplateAttachmentSummary(
            id = id!!,
            fileName = fileName,
            contentType = contentType,
            size = size,
        )

    private fun TemplateAttachmentDocument.toContent(): TemplateAttachmentContent =
        TemplateAttachmentContent(
            id = id!!,
            fileName = fileName,
            contentType = contentType,
            content = content,
        )

    companion object {
        const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024
    }
}
