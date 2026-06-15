package com.contactfirstio.structuredproducts.dsl

internal fun Map<String, Any>.requireNumber(key: String): Double =
    when (val value = this[key]) {
        is Number -> value.toDouble()
        null -> throw IllegalArgumentException("Missing required leg parameter: $key")
        else -> throw IllegalArgumentException("Leg parameter '$key' must be numeric, got ${value::class.simpleName}")
    }
