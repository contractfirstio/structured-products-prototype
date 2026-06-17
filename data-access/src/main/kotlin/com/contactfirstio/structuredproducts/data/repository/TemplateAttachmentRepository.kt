package com.contactfirstio.structuredproducts.data.repository

import com.contactfirstio.structuredproducts.data.document.TemplateAttachmentDocument
import org.springframework.data.mongodb.repository.MongoRepository

interface TemplateAttachmentRepository : MongoRepository<TemplateAttachmentDocument, String> {
    fun findByTemplateId(templateId: String): List<TemplateAttachmentDocument>
}
