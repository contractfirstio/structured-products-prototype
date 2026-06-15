package com.contactfirstio.structuredproducts.dsl

@DslMarker
annotation class FinancialContractDsl

@FinancialContractDsl
class FinancialContractBuilder(
    private val legProcessors: List<LegProcessor>,
) {
    var underlying: String = ""
    var maturityMonths: Int = 0

    private val legs = mutableListOf<ProductLeg>()

    fun legs(legData: Map<String, Any>) {
        legData.forEach { (legType, rawParameters) ->
            val processor = legProcessors.firstOrNull { it.legType == legType }
                ?: throw IllegalArgumentException("No LegProcessor registered for leg type '$legType'")

            @Suppress("UNCHECKED_CAST")
            val parameters = rawParameters as? Map<String, Any>
                ?: throw IllegalArgumentException(
                    "Leg '$legType' parameters must be a Map<String, Any>, got ${rawParameters?.let { it::class.simpleName }}",
                )

            legs.add(processor.process(parameters))
        }
    }

    fun build(): FinancialContract {
        require(underlying.isNotBlank()) { "Underlying is required" }
        require(maturityMonths > 0) { "Maturity months must be positive, got $maturityMonths" }
        require(legs.isNotEmpty()) { "At least one product leg is required" }
        return FinancialContract(
            underlying = underlying,
            maturityMonths = maturityMonths,
            legs = legs.toList(),
        )
    }
}

fun financialContract(
    legData: Map<String, Any>,
    legProcessors: List<LegProcessor> = defaultLegProcessors(),
    block: FinancialContractBuilder.() -> Unit,
): FinancialContract =
    FinancialContractBuilder(legProcessors).apply {
        block()
        legs(legData)
    }.build()

fun defaultLegProcessors(): List<LegProcessor> =
    listOf(ProtectionLegProcessor(), UpsideLegProcessor())
