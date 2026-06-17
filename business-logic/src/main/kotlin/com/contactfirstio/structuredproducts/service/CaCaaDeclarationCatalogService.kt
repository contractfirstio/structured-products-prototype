package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.catalog.CaCaaDeclarationCatalog
import com.contactfirstio.structuredproducts.catalog.CaCaaDeclarationQuestion
import org.springframework.stereotype.Service

@Service
class CaCaaDeclarationCatalogService {

    fun questions(): List<CaCaaDeclarationQuestion> = CaCaaDeclarationCatalog.questions

    fun findByKey(key: String): CaCaaDeclarationQuestion? = questions().firstOrNull { it.key == key }

    fun validateIncludedQuestionKeys(keys: Set<String>) {
        val knownKeys = questions().map { it.key }.toSet()
        val unknown = keys - knownKeys
        if (unknown.isNotEmpty()) {
            throw ValidationException("Unknown CA/CAA declaration question keys: ${unknown.joinToString()}")
        }
    }
}
