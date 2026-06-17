package com.contactfirstio.structuredproducts.data.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "template_attachments")
data class TemplateAttachmentDocument(
    @Id val id: String? = null,
    val templateId: String? = null,
    val fieldKey: String,
    val fileName: String,
    val contentType: String,
    val size: Long,
    val content: ByteArray,
    val uploadedAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TemplateAttachmentDocument

        if (id != other.id) return false
        if (templateId != other.templateId) return false
        if (fieldKey != other.fieldKey) return false
        if (fileName != other.fileName) return false
        if (contentType != other.contentType) return false
        if (size != other.size) return false
        if (!content.contentEquals(other.content)) return false
        if (uploadedAt != other.uploadedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (templateId?.hashCode() ?: 0)
        result = 31 * result + fieldKey.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + uploadedAt.hashCode()
        return result
    }
}
