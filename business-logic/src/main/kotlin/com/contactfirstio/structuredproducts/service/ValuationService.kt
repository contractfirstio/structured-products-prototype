package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.ProductDocument
import com.contactfirstio.structuredproducts.dsl.FinancialContract
import com.contactfirstio.structuredproducts.dsl.financialContract
import org.springframework.stereotype.Service

@Service
class ValuationService {

    fun hydrateContract(product: ProductDocument): FinancialContract {
        try {
            return financialContract(toLegData(product.legs)) {
                underlying = product.underlying
                maturityMonths = product.maturityMonths
            }
        } catch (ex: RuntimeException) {
            throw ContractHydrationException(
                ex.message ?: "Failed to hydrate financial contract from product document",
                ex,
            )
        }
    }

    private fun toLegData(legs: List<Map<String, Any>>): Map<String, Any> {
        if (legs.isEmpty()) {
            throw IllegalArgumentException("At least one product leg is required")
        }

        return legs.associate { leg ->
            val type = leg["type"] as? String
                ?: throw IllegalArgumentException("Each leg must include a 'type' field")

            val parameters = leg.filterKeys { it != "type" }
            if (parameters.isEmpty()) {
                throw IllegalArgumentException("Leg '$type' must include parameter fields")
            }

            type to parameters
        }
    }
}
