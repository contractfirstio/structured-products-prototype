package com.contactfirstio.structuredproducts.e2e

import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class ValidationE2ETest : DatabaseCleanupE2ETest() {

    @Test
    fun createProductTypeRequiresName() {
        withPage("/admin/create-type") { page ->
            page.getByRole(AriaRole.BUTTON, com.microsoft.playwright.Page.GetByRoleOptions().setName("Save Product Type")).click()
            page.waitForNotification("Product type name is required")
        }
        assertEquals(0, productTypeRepository.count())
    }

    @Test
    fun createProductRequiresCompletedFields() {
        seedPpnProductType()

        withPage("/create-product") { page ->
            page.selectProductType(PPN_TYPE_NAME)
            assertThat(page.getByRole(AriaRole.BUTTON, com.microsoft.playwright.Page.GetByRoleOptions().setName("Submit"))).isEnabled()
            page.submitProductForm()
            page.waitForNotification("Please complete all required fields")
        }
        assertEquals(0, productRepository.count())
    }

    @Test
    fun orderEntryRequiresProductAndNotional() {
        withPage("/order-entry") { page ->
            page.getByRole(AriaRole.BUTTON, com.microsoft.playwright.Page.GetByRoleOptions().setName("Submit Order")).click()
            page.waitForNotification("Please select a product and enter a notional amount")
        }
        assertEquals(0, orderRepository.count())
    }

    @Test
    fun draftProductsAreNotAvailableForOrderEntry() {
        seedPpnProductType()

        withPage("/create-product") { page ->
            page.selectProductType(PPN_TYPE_NAME)
            page.fillPpnProductForm(
                underlying = "TSLA",
                maturityMonths = "18",
                protectionLevel = "100",
            )
            page.submitProductForm()
            page.waitForNotification("Product saved as DRAFT")
        }

        withPage("/order-entry") { page ->
            page.getByLabel("Active Product").click()
            assertThat(page.locator("vaadin-combo-box-item").filter(
                com.microsoft.playwright.Locator.FilterOptions().setHasText("TSLA"),
            )).hasCount(0)
        }

        assertEquals(1, productRepository.findByStatus(ProductStatus.DRAFT).size)
        assertTrue(orderRepository.findAll().isEmpty())
    }

    private fun seedPpnProductType() {
        withPage("/admin/create-type") { page ->
            page.createPpnProductType()
        }
    }
}
