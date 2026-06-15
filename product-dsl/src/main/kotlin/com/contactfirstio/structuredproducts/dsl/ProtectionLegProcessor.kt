package com.contactfirstio.structuredproducts.dsl

class ProtectionLegProcessor : LegProcessor {
    override val legType: String = "protection"

    override fun process(parameters: Map<String, Any>): ProductLeg {
        val level = parameters.requireNumber("level")
        require(level in 0.0..100.0) { "Protection level must be between 0 and 100, got $level" }
        return ProtectionLeg(level)
    }
}
