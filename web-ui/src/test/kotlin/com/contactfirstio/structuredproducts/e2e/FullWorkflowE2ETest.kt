package com.contactfirstio.structuredproducts.e2e

import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class FullWorkflowE2ETest : DatabaseCleanupE2ETest() {

    @Test
    fun completeDeskLifecycleFromTypeCreationThroughBlotterCleanup() {
        withPage("/admin/create-type") { page ->
            page.createPpnProductType("Autocall Note")
        }

        withPage("/create-product") { page ->
            page.selectProductType("Autocall Note")
            page.fillPpnProductForm("SPX", "36", "100")
            page.submitProductForm()
            page.waitForNotification("Product saved as DRAFT")
        }

        assertEquals(ProductStatus.DRAFT, productRepository.findAll().single().status)

        withPage("/create-product") { page ->
            page.selectProductType("Autocall Note")
            page.fillPpnProductForm("SPX", "36", "100", "125")
            page.submitProductForm()
            page.waitForNotification("Product saved as ACTIVE")
        }

        assertEquals(2, productRepository.count())
        assertEquals(1, productRepository.findByStatus(ProductStatus.ACTIVE).size)

        withPage("/order-entry") { page ->
            page.submitOrder("SPX", "100000")
            page.waitForNotification("Order submitted for SPX")
        }

        val typeId = productTypeRepository.findAll().single().id!!
        val draftProductId = productRepository.findByStatus(ProductStatus.DRAFT).single().id!!
        val activeProductId = productRepository.findByStatus(ProductStatus.ACTIVE).single().id!!
        val orderId = orderRepository.findAll().single().id!!

        withPage("/blotter") { page ->
            assertThat(page.blotterSection("Product Types (Level 1)").gridCellWith("Autocall Note")).isVisible()
            assertThat(page.blotterSection("Product Instances (Level 2)").gridCellWith("DRAFT")).isVisible()
            assertThat(page.blotterSection("Product Instances (Level 2)").gridCellWith("ACTIVE")).isVisible()
            assertThat(page.blotterSection("Orders (Level 3)").gridCellWith("SPX")).isVisible()

            page.clickBlotterDelete("order", orderId)
            page.confirmDeleteDialog()

            page.clickBlotterDelete("product-instance", draftProductId)
            page.confirmDeleteDialog()

            page.clickBlotterDelete("product-instance", activeProductId)
            page.confirmDeleteDialog()

            page.clickBlotterDelete("product-type", typeId)
            page.confirmDeleteDialog()
        }

        assertEquals(0, productTypeRepository.count())
        assertEquals(0, productRepository.count())
        assertEquals(0, orderRepository.count())
    }
}
