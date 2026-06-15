package com.contactfirstio.structuredproducts.e2e

import com.contactfirstio.structuredproducts.data.document.ProductStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class DynamicFormE2ETest : DatabaseCleanupE2ETest() {

    @Test
    fun rendersAllFieldTypesFromLegSchema() {
        withPage("/admin/leg-builder") { it.createFullFeatureLegSchema() }
        withPage("/admin/create-type") {
            it.createProductTypeWithSingleLeg(ALL_TYPES_PRODUCT_TYPE, FULL_FEATURE_LEG_NAME)
        }

        withPage("/create-product") { page ->
            page.selectProductType(ALL_TYPES_PRODUCT_TYPE)
            page.getByLabel("Notes").waitFor()
            page.getByLabel("Barrier Level").waitFor()
            page.getByLabel("Observation Count").waitFor()
            page.getByLabel("Autocallable").waitFor()
            page.getByLabel("Payoff Style").waitFor()
        }
    }

    @Test
    fun persistsCapturedLegDataAsJsonMaps() {
        withPage("/admin/leg-builder") { it.createFullFeatureLegSchema() }
        withPage("/admin/create-type") {
            it.createProductTypeWithSingleLeg(ALL_TYPES_PRODUCT_TYPE, FULL_FEATURE_LEG_NAME)
        }

        withPage("/create-product") { page ->
            page.selectProductType(ALL_TYPES_PRODUCT_TYPE)
            page.fillFullFeatureProductForm(
                underlying = "EUR/USD",
                maturityMonths = "36",
                notes = "Desk RFQ",
                barrierLevel = "105.5",
                observationCount = "12",
                autocallable = true,
                payoffStyle = "European",
            )
            page.submitProductForm()
            page.waitForNotification("Product saved as ACTIVE")
        }

        val product = productRepository.findAll().single()
        assertEquals(ProductStatus.ACTIVE, product.status)
        assertEquals("EUR/USD", product.underlying)
        assertEquals(36, product.maturityMonths)

        val leg = product.legs.single()
        assertTrue(leg.containsKey("legSchemaId"))
        assertEquals("Desk RFQ", leg["Notes"])
        assertEquals(105.5, (leg["Barrier Level"] as Number).toDouble())
        assertEquals(12, (leg["Observation Count"] as Number).toInt())
        assertEquals(true, leg["Autocallable"])
        assertEquals("European", leg["Payoff Style"])
    }

    @Test
    fun optionalFieldBlankSavesAsDraft() {
        withPage("/admin/leg-builder") { it.createFullFeatureLegSchema() }
        withPage("/admin/create-type") {
            it.createProductTypeWithSingleLeg(ALL_TYPES_PRODUCT_TYPE, FULL_FEATURE_LEG_NAME)
        }

        withPage("/create-product") { page ->
            page.selectProductType(ALL_TYPES_PRODUCT_TYPE)
            page.fillFullFeatureProductForm(
                underlying = "SPX",
                maturityMonths = "24",
                notes = "Partial RFQ",
                barrierLevel = "100",
                observationCount = "4",
                autocallable = false,
                payoffStyle = null,
            )
            page.submitProductForm()
            page.waitForNotification("Product saved as DRAFT")
        }

        val product = productRepository.findAll().single()
        assertEquals(ProductStatus.DRAFT, product.status)
        val leg = product.legs.single()
        assertFalse(leg.containsKey("Payoff Style"))
    }
}
