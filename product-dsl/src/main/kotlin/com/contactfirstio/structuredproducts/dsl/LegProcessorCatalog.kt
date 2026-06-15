package com.contactfirstio.structuredproducts.dsl

data class LegProcessorDefinition(
    val schemaLegType: String,
    val processorLegType: String,
    val displayName: String,
    val parameterKey: String,
    val parameterLabel: String,
)

object LegProcessorCatalog {
    val definitions: List<LegProcessorDefinition> = listOf(
        LegProcessorDefinition(
            schemaLegType = "ProtectionLeg",
            processorLegType = "protection",
            displayName = "Protection Leg",
            parameterKey = "level",
            parameterLabel = "Protection Level (%)",
        ),
        LegProcessorDefinition(
            schemaLegType = "UpsideLeg",
            processorLegType = "upside",
            displayName = "Upside Leg",
            parameterKey = "participation",
            parameterLabel = "Participation Rate (%)",
        ),
    )

    fun findBySchemaLegType(schemaLegType: String): LegProcessorDefinition? =
        definitions.firstOrNull { it.schemaLegType == schemaLegType }

    fun findByProcessorLegType(processorLegType: String): LegProcessorDefinition? =
        definitions.firstOrNull { it.processorLegType == processorLegType }

    fun resolveProcessorLegType(schemaLegType: String): String =
        findBySchemaLegType(schemaLegType)?.processorLegType
            ?: throw IllegalArgumentException("Unknown schema leg type: $schemaLegType")
}
