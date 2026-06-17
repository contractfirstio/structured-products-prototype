package com.contactfirstio.structuredproducts.catalog

object CaCaaDeclarationCatalog {

    val questions: List<CaCaaDeclarationQuestion> =
        listOf(
            question(
                key = "risk_disclosure_acknowledged",
                question = "Does the client confirm they have received and understood the risk disclosure?",
            ),
            question(
                key = "product_suitability_confirmed",
                question = "Does the client confirm the product is suitable for their investment profile?",
            ),
            question(
                key = "brochure_read",
                question = "Does the client confirm they have read the product brochure?",
            ),
            question(
                key = "notional_terms_understood",
                question = "Does the client confirm understanding of the notional amount and settlement terms?",
            ),
            question(
                key = "no_undocumented_advice",
                question = "Does the client confirm they are not relying on undocumented third-party advice?",
            ),
            question(
                key = "authorized_signatory",
                question = "Does the client confirm the signatory is authorized to enter this transaction?",
            ),
        )

    private fun question(key: String, question: String): CaCaaDeclarationQuestion =
        CaCaaDeclarationQuestion(key = key, question = question)
}
