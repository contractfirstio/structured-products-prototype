package com.contactfirstio.structuredproducts.dsl

sealed interface ProductLeg

data class ProtectionLeg(val levelPercent: Double) : ProductLeg

data class UpsideLeg(val participationPercent: Double) : ProductLeg
