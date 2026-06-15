package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.FieldDataType

data class GlobalTermsSchemaDto(
    val requiresUnderlying: Boolean,
    val requiresMaturityDate: Boolean,
)

data class FieldDefinitionDto(
    val fieldName: String,
    val dataType: FieldDataType,
    val isRequired: Boolean,
    val enumOptions: List<String> = emptyList(),
)

data class LegSchemaDetail(
    val id: String,
    val name: String,
    val fields: List<FieldDefinitionDto>,
)

data class ProductTypeDetail(
    val id: String,
    val name: String,
    val globalTermsSchema: GlobalTermsSchemaDto,
    val allowedLegSchemaIds: List<String>,
    val legSchemas: List<LegSchemaDetail>,
)

data class CreateLegSchemaCommand(
    val name: String,
    val fields: List<FieldDefinitionDto>,
)

data class CreateProductTypeCommand(
    val name: String,
    val globalTermsSchema: GlobalTermsSchemaDto,
    val allowedLegSchemaIds: List<String>,
)

data class CreateProductInstanceCommand(
    val typeId: String,
    val underlying: String?,
    val maturityMonths: Int?,
    val legData: List<Map<String, Any>>,
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
    val legData: List<Map<String, Any>>,
)

data class SavedProductResult(
    val id: String,
    val status: String,
    val underlying: String,
)
