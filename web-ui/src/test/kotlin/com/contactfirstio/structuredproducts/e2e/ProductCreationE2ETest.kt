package com.contactfirstio.structuredproducts.e2e

import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class ProductCreationE2ETest : DatabaseCleanupE2ETest() {

    @Test
    fun submitDisabledUntilProductTypeSelected() {
        withPage("/create-product") { page ->
            assertThat(
                page.getByRole(
                    com.microsoft.playwright.options.AriaRole.BUTTON,
                    com.microsoft.playwright.Page.GetByRoleOptions().setName("Submit"),
                ),
            ).isDisabled()
        }
    }

    @Test
    fun rfqFlowCreatesDraftProduct() {
        seedPpnProductType()

        withPage("/create-product") { page ->
            page.selectProductType(PPN_TYPE_NAME)
            page.fillPpnProductForm(
                underlying = "AAPL",
                maturityMonths = "24",
                protectionLevel = "100",
            )
            page.submitProductForm()
            page.waitForNotification("Product saved as DRAFT")
        }

        val draftProducts = productRepository.findByStatus(ProductStatus.DRAFT)
        assertEquals(1, draftProducts.size)
        assertEquals("AAPL", draftProducts.first().underlying)

        val leg = draftProducts.first().legs.first()
        assertTrue(leg.containsKey("legSchemaId"))
        assertEquals(100.0, (leg["Protection Level (%)"] as Number).toDouble())
        assertFalse(leg.containsKey("Participation Rate (%)"))
    }

    @Test
    fun activeProductCanBeOrdered() {
        seedPpnProductType()
        seedActiveProduct("NVDA", "12", "90", "150")

        withPage("/order-entry") { page ->
            page.submitOrder("NVDA", "50000")
            page.waitForNotification("Order submitted for NVDA")
        }

        val activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE)
        assertEquals(1, activeProducts.size)
        assertTrue(orderRepository.findAll().any { it.productId == activeProducts.first().id })
    }

    @Test
    fun completingOptionalFieldsPromotesToActive() {
        seedPpnProductType()
        seedDraftProduct("SPX", "36", "100")

        withPage("/create-product") { page ->
            page.selectProductType(PPN_TYPE_NAME)
            page.fillPpnProductForm("SPX", "36", "100", "125")
            page.submitProductForm()
            page.waitForNotification("Product saved as ACTIVE")
        }

        assertEquals(2, productRepository.count())
        assertEquals(1, productRepository.findByStatus(ProductStatus.DRAFT).size)
        assertEquals(1, productRepository.findByStatus(ProductStatus.ACTIVE).size)
    }
}
