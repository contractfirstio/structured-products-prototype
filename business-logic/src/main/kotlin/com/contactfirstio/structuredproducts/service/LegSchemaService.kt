package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.FieldDefinition
import com.contactfirstio.structuredproducts.data.document.LegSchemaDocument
import com.contactfirstio.structuredproducts.data.repository.LegSchemaRepository
import org.springframework.stereotype.Service

@Service
class LegSchemaService(
    private val legSchemaRepository: LegSchemaRepository,
) {

    fun findAll(): List<LegSchemaDetail> =
        legSchemaRepository.findAll().map { it.toDetail() }

    fun findById(id: String): LegSchemaDetail? =
        legSchemaRepository.findById(id).orElse(null)?.toDetail()

    fun findByIds(ids: List<String>): List<LegSchemaDetail> {
        if (ids.isEmpty()) return emptyList()
        val byId = legSchemaRepository.findAllById(ids).associateBy { it.id!! }
        return ids.mapNotNull { byId[it]?.toDetail() }
    }

    fun save(command: CreateLegSchemaCommand): LegSchemaDetail {
        if (command.name.isBlank()) {
            throw ValidationException("Leg schema name is required")
        }
        if (command.fields.isEmpty()) {
            throw ValidationException("At least one field definition is required")
        }

        command.fields.forEach { field ->
            validateFieldDefinition(field)
        }

        val saved = legSchemaRepository.save(
            LegSchemaDocument(
                name = command.name.trim(),
                fields = command.fields.map { it.toDocument() },
            ),
        )

        return saved.toDetail()
    }

    fun update(id: String, command: CreateLegSchemaCommand): LegSchemaDetail {
        val existing = legSchemaRepository.findById(id)
            .orElseThrow { ValidationException("Leg schema not found: $id") }

        if (command.name.isBlank()) {
            throw ValidationException("Leg schema name is required")
        }
        if (command.fields.isEmpty()) {
            throw ValidationException("At least one field definition is required")
        }

        command.fields.forEach { field ->
            validateFieldDefinition(field)
        }

        val saved = legSchemaRepository.save(
            existing.copy(
                name = command.name.trim(),
                fields = command.fields.map { it.toDocument() },
            ),
        )

        return saved.toDetail()
    }

    private fun validateFieldDefinition(field: FieldDefinitionDto) {
        if (field.fieldName.isBlank()) {
            throw ValidationException("Field name is required")
        }
        if (field.dataType == FieldDataType.ENUM && field.enumOptions.isEmpty()) {
            throw ValidationException("ENUM field '${field.fieldName}' requires at least one option")
        }
    }

    private fun FieldDefinitionDto.toDocument(): FieldDefinition =
        FieldDefinition(
            fieldName = fieldName.trim(),
            dataType = dataType,
            isRequired = isRequired,
            enumOptions = enumOptions.map { it.trim() }.filter { it.isNotEmpty() },
        )

    private fun LegSchemaDocument.toDetail(): LegSchemaDetail =
        LegSchemaDetail(
            id = id!!,
            name = name,
            fields = fields.map { field ->
                FieldDefinitionDto(
                    fieldName = field.fieldName,
                    dataType = field.dataType,
                    isRequired = field.isRequired,
                    enumOptions = field.enumOptions,
                )
            },
        )
}
