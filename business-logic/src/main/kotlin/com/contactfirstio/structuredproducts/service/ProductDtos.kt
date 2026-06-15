package com.contactfirstio.structuredproducts.service

data class GlobalTermsSchemaDto(
    val requiresUnderlying: Boolean,
    val requiresMaturityDate: Boolean,
)

data class LegSchemaDto(
    val legType: String,
    val isRequired: Boolean,
    val parameterLabel: String,
    val processorLegType: String,
)

data class ProductTypeDetail(
    val id: String,
    val name: String,
    val globalTermsSchema: GlobalTermsSchemaDto,
    val legSchemas: List<LegSchemaDto>,
)

data class LegProcessorOption(
    val schemaLegType: String,
    val displayName: String,
)

data class CreateProductTypeCommand(
    val name: String,
    val globalTermsSchema: GlobalTermsSchemaDto,
    val legSchemas: List<LegSchemaDto>,
)

data class CreateProductInstanceCommand(
    val typeId: String,
    val underlying: String?,
    val maturityMonths: Int?,
    val legValues: Map<String, Double?>,
)

data class OrderEditDetail(
    val id: String,
    val productId: String,
    val notionalInvested: Double,
    val activeProducts: List<ActiveProductSummary>,
)

data class ProductInstanceEditDetail(
    val id: String,
    val typeId: String,
    val productType: ProductTypeDetail,
    val underlying: String?,
    val maturityMonths: Int?,
    val legValues: Map<String, Double?>,
)

data class SavedProductResult(
    val id: String,
    val status: String,
    val underlying: String,
)
