package com.contactfirstio.structuredproducts.data.repository

import com.contactfirstio.structuredproducts.data.document.ProductTemplateDocument
import org.springframework.data.mongodb.repository.MongoRepository

interface ProductTemplateRepository : MongoRepository<ProductTemplateDocument, String>
