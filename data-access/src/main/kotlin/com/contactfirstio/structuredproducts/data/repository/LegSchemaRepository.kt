package com.contactfirstio.structuredproducts.data.repository

import com.contactfirstio.structuredproducts.data.document.LegSchemaDocument
import org.springframework.data.mongodb.repository.MongoRepository

interface LegSchemaRepository : MongoRepository<LegSchemaDocument, String>
