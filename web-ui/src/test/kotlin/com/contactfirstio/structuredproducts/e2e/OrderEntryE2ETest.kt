package com.contactfirstio.structuredproducts.e2e

import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class OrderEntryE2ETest : DatabaseCleanupE2ETest() {

    @Test
    fun orderEntryPageLoads() {
        withPage("/order-entry") { page ->
            assertThat(page.getByText("Order Entry").first()).isVisible()
            assertThat(page.getByLabel("Active Product")).isVisible()
            assertThat(page.getByLabel("Notional Amount")).isVisible()
        }
    }

    @Test
    fun submitRequiresProductAndNotional() {
        withPage("/order-entry") { page ->
            page.clickButton("Submit Order")
            page.waitForNotification("Please select a product and enter a notional amount")
        }
        assertEquals(0, orderRepository.count())
    }

    @Test
    fun submitsOrderAgainstActiveProduct() {
        seedPpnProductType()
        seedActiveProduct("NVDA", "12", "90", "150")

        withPage("/order-entry") { page ->
            page.submitOrder("NVDA", "50000")
            page.waitForNotification("Order submitted for NVDA")
        }

        val order = orderRepository.findAll().single()
        assertEquals(50_000.0, order.notionalInvested)
        assertEquals(productRepository.findAll().single().id, order.productId)
    }

    @Test
    fun draftProductsAreExcludedFromActiveProductList() {
        seedPpnProductType()
        seedDraftProduct("TSLA", "18", "100")

        withPage("/order-entry") { page ->
            page.getByLabel("Active Product").click()
            assertThat(page.locator("vaadin-combo-box-item").filter(
                com.microsoft.playwright.Locator.FilterOptions().setHasText("TSLA"),
            )).hasCount(0)
        }

        assertEquals(1, productRepository.findByStatus(ProductStatus.DRAFT).size)
        assertTrue(orderRepository.findAll().isEmpty())
    }

    @Test
    fun listsMultipleActiveProducts() {
        seedPpnProductType()
        seedActiveProduct("AAPL", "12", "100", "120")
        seedActiveProduct("GOOG", "24", "95", "110")

        withPage("/order-entry") { page ->
            page.getByLabel("Active Product").click()
            assertThat(page.locator("vaadin-combo-box-item").filter(
                com.microsoft.playwright.Locator.FilterOptions().setHasText("AAPL"),
            )).isVisible()
            assertThat(page.locator("vaadin-combo-box-item").filter(
                com.microsoft.playwright.Locator.FilterOptions().setHasText("GOOG"),
            )).isVisible()
        }

        assertEquals(2, productRepository.findByStatus(ProductStatus.ACTIVE).size)
    }
}
