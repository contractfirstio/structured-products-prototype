package com.contactfirstio.structuredproducts.catalog

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.FieldKind
import com.contactfirstio.structuredproducts.data.document.FieldRequirement

data class CommonFieldGroupSpec(
    val baseKey: String,
    val displayName: String,
    val category: String,
    val dataType: FieldDataType = FieldDataType.STRING,
    val requirement: FieldRequirement = FieldRequirement.OPTIONAL,
    val enumOptions: List<String> = emptyList(),
)

object CommonFieldGroup {
    private val optionRequirementOptions = listOf("Required", "Not Required", "Optional")
    private val bexLogicOptions = listOf("N/A", "Auto-Generate", "Manual Entry", "System Override")
    private val emailConfOptions = listOf("True", "False")

    fun fields(spec: CommonFieldGroupSpec): List<CatalogFieldDefinition> =
        listOf(
            CatalogFieldBuilder.field(
                kind = FieldKind.COMMON,
                key = "${spec.baseKey}_option",
                category = spec.category,
                displayName = "${spec.displayName} - Option",
                dataType = FieldDataType.STRING,
                requirement = FieldRequirement.MANDATORY,
                enumOptions = optionRequirementOptions,
            ),
            CatalogFieldBuilder.field(
                kind = FieldKind.COMMON,
                key = "${spec.baseKey}_in_email_conf",
                category = spec.category,
                displayName = "${spec.displayName} - In email conf",
                dataType = FieldDataType.BOOLEAN,
                requirement = FieldRequirement.OPTIONAL,
                enumOptions = emailConfOptions,
            ),
            CatalogFieldBuilder.field(
                kind = FieldKind.COMMON,
                key = "${spec.baseKey}_bex_logic",
                category = spec.category,
                displayName = "${spec.displayName} - BEX Logic",
                dataType = FieldDataType.STRING,
                requirement = FieldRequirement.MANDATORY,
                enumOptions = bexLogicOptions,
            ),
            CatalogFieldBuilder.field(
                kind = FieldKind.COMMON,
                key = spec.baseKey,
                category = spec.category,
                displayName = spec.displayName,
                dataType = spec.dataType,
                requirement = spec.requirement,
                enumOptions = spec.enumOptions,
            ),
        )
}
