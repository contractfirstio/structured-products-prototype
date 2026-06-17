package com.contactfirstio.structuredproducts.service

data class TemplateAttachmentSummary(
    val id: String,
    val fileName: String,
    val contentType: String,
    val size: Long,
)

data class TemplateAttachmentContent(
    val id: String,
    val fileName: String,
    val contentType: String,
    val content: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TemplateAttachmentContent

        if (id != other.id) return false
        if (fileName != other.fileName) return false
        if (contentType != other.contentType) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}
