package com.contactfirstio.structuredproducts.dsl

data class FinancialContract(
    val underlying: String,
    val maturityMonths: Int,
    val legs: List<ProductLeg>,
)
