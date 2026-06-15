package com.contactfirstio.structuredproducts.service

data class ProductTypeBlotterRow(
    val id: String,
    val name: String,
    val requiresUnderlying: String,
    val requiresMaturity: String,
    val legsSummary: String,
    val canEdit: Boolean,
    val canDelete: Boolean,
)

data class ProductInstanceBlotterRow(
    val id: String,
    val typeName: String,
    val underlying: String,
    val maturityMonths: Int,
    val status: String,
    val legsSummary: String,
    val canEdit: Boolean,
    val canDelete: Boolean,
)

data class OrderBlotterRow(
    val id: String,
    val productId: String,
    val productUnderlying: String,
    val notionalInvested: Double,
    val status: String,
    val canEdit: Boolean,
    val canDelete: Boolean,
)

data class BlotterSnapshot(
    val productTypes: List<ProductTypeBlotterRow>,
    val productInstances: List<ProductInstanceBlotterRow>,
    val orders: List<OrderBlotterRow>,
)
