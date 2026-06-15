package com.contactfirstio.structuredproducts.dsl

class UpsideLegProcessor : LegProcessor {
    override val legType: String = "upside"

    override fun process(parameters: Map<String, Any>): ProductLeg {
        val participation = parameters.requireNumber("participation")
        require(participation > 0.0) { "Upside participation must be positive, got $participation" }
        return UpsideLeg(participation)
    }
}
