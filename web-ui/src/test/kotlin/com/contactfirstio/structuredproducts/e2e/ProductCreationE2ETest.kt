package com.contactfirstio.structuredproducts.e2e

import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class ProductCreationE2ETest : DatabaseCleanupE2ETest() {

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
    }

    @Test
    fun activeProductCanBeOrdered() {
        seedPpnProductType()

        withPage("/create-product") { page ->
            page.selectProductType(PPN_TYPE_NAME)
            page.fillPpnProductForm(
                underlying = "NVDA",
                maturityMonths = "12",
                protectionLevel = "90",
                participationRate = "150",
            )
            page.submitProductForm()
            page.waitForNotification("Product saved as ACTIVE")
        }

        withPage("/order-entry") { page ->
            page.submitOrder("NVDA", "50000")
            page.waitForNotification("Order submitted for NVDA")
        }

        val activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE)
        assertEquals(1, activeProducts.size)
        assertTrue(orderRepository.findAll().any { it.productId == activeProducts.first().id })
    }

    private fun seedPpnProductType() {
        withPage("/admin/create-type") { page ->
            page.createPpnProductType()
        }
    }
}
