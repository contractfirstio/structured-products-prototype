package com.contactfirstio.structuredproducts.data.repository

import com.contactfirstio.structuredproducts.data.document.ProductTypeDocument
import org.springframework.data.mongodb.repository.MongoRepository

interface ProductTypeRepository : MongoRepository<ProductTypeDocument, String>
