package com.contactfirstio.structuredproducts.service

data class ActiveProductSummary(
    val id: String,
    val underlying: String,
    val maturityMonths: Int,
)
