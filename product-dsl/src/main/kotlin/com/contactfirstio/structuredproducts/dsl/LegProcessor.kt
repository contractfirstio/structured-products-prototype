package com.contactfirstio.structuredproducts.dsl

interface LegProcessor {
    val legType: String

    fun process(parameters: Map<String, Any>): ProductLeg
}
