package com.contactfirstio.structuredproducts.e2e

import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class BlotterE2ETest : DatabaseCleanupE2ETest() {

    @Test
    fun blotterDisplaysLegSchemas() {
        withPage("/admin/leg-builder") { it.createProtectionLegSchema() }

        withPage("/blotter") { page ->
            val legSchemas = page.blotterSection("Leg Schemas (Level 1 · Admin)")
            assertThat(legSchemas.gridCellWith(PROTECTION_LEG_NAME)).isVisible()
            assertThat(legSchemas.gridCellWith("Protection Level (%)")).isVisible()
        }
    }

    @Test
    fun editLegSchemaWhenUnreferenced() {
        withPage("/admin/leg-builder") { it.createProtectionLegSchema() }
        val legId = legSchemaRepository.findAll().single().id!!

        withPage("/blotter") { page ->
            page.clickBlotterEdit("leg-schema", legId)
            page.waitForDialogTitle("Edit Leg Schema")
            page.fillDialogField("Leg Name", "Enhanced Protection Leg")
            page.saveDialog()
            page.waitForNotification("Leg schema updated")

            assertThat(page.blotterSection("Leg Schemas (Level 1 · Admin)").gridCellWith("Enhanced Protection Leg")).isVisible()
        }

        assertEquals("Enhanced Protection Leg", legSchemaRepository.findAll().single().name)
    }

    @Test
    fun deleteLegSchemaWhenUnreferenced() {
        withPage("/admin/leg-builder") { it.createProtectionLegSchema() }
        val legId = legSchemaRepository.findAll().single().id!!

        withPage("/blotter") { page ->
            page.clickBlotterDelete("leg-schema", legId)
            page.confirmDeleteDialog()
        }

        assertEquals(0, legSchemaRepository.count())
    }

    @Test
    fun referentialIntegrityProtectsReferencedLegSchemas() {
        seedPpnProductType()
        val protectionLegId = legSchemaRepository.findAll().first { it.name == PROTECTION_LEG_NAME }.id!!
        val upsideLegId = legSchemaRepository.findAll().first { it.name == UPSIDE_LEG_NAME }.id!!

        withPage("/blotter") { page ->
            page.expectBlotterActionProtected("leg-schema", protectionLegId, "edit")
            page.expectBlotterActionProtected("leg-schema", protectionLegId, "delete")
            page.expectBlotterActionProtected("leg-schema", upsideLegId, "edit")
            page.expectBlotterActionProtected("leg-schema", upsideLegId, "delete")
        }
    }

    @Test
    fun blotterDisplaysCreatedHierarchy() {
        seedFullHierarchy()

        withPage("/blotter") { page ->
            val types = page.blotterSection("Product Types (Level 1)")
            val instances = page.blotterSection("Product Instances (Level 2)")
            val orders = page.blotterSection("Orders (Level 3)")

            assertThat(types.gridCellWith(PPN_TYPE_NAME)).isVisible()
            assertThat(types.gridCellWith(PROTECTION_LEG_NAME)).isVisible()
            assertThat(types.gridCellWith(UPSIDE_LEG_NAME)).isVisible()
            assertThat(instances.gridCellWith("MSFT")).isVisible()
            assertThat(instances.gridCellWith("ACTIVE")).isVisible()
            assertThat(orders.gridCellWith("MSFT")).isVisible()
            assertThat(orders.gridCellWith("SUBMITTED")).isVisible()
        }
    }

    @Test
    fun blotterRefreshKeepsLoadedData() {
        seedPpnProductType()
        seedDraftProduct("GOOG", "24", "100")

        withPage("/blotter") { page ->
            val instances = page.blotterSection("Product Instances (Level 2)")
            assertThat(instances.gridCellWith("GOOG")).isVisible()
            page.getByText("Refresh").click()
            assertThat(instances.gridCellWith("GOOG")).isVisible()
        }
    }

    @Test
    fun editProductTypeWhenUnreferenced() {
        seedPpnProductType()
        val typeId = productTypeRepository.findAll().single().id!!

        withPage("/blotter") { page ->
            page.clickBlotterEdit("product-type", typeId)
            page.waitForDialogTitle("Edit Product Type")
            page.fillDialogField("Product Type Name", "Enhanced PPN")
            page.saveDialog()
            page.waitForNotification("Product type updated")

            assertThat(page.blotterSection("Product Types (Level 1)").gridCellWith("Enhanced PPN")).isVisible()
        }

        assertEquals("Enhanced PPN", productTypeRepository.findAll().single().name)
    }

    @Test
    fun editProductInstanceWhenUnreferenced() {
        seedPpnProductType()
        seedActiveProduct("AMD", "12", "95", "120")
        val productId = productRepository.findAll().single().id!!

        withPage("/blotter") { page ->
            page.clickBlotterEdit("product-instance", productId)
            page.waitForDialogTitle("Edit Product Instance")
            page.fillDialogField("Underlying", "AMD.X")
            page.saveDialog()
            page.waitForNotification("Product instance updated")

            assertThat(page.blotterSection("Product Instances (Level 2)").gridCellWith("AMD.X")).isVisible()
        }

        assertEquals("AMD.X", productRepository.findAll().single().underlying)
    }

    @Test
    fun editOrderUpdatesNotional() {
        seedFullHierarchy()
        val orderId = orderRepository.findAll().single().id!!

        withPage("/blotter") { page ->
            page.clickBlotterEdit("order", orderId)
            page.waitForDialogTitle("Edit Order")
            page.fillDialogField("Notional Amount", "75000")
            page.saveDialog()
            page.waitForNotification("Order updated")
        }

        assertEquals(75_000.0, orderRepository.findAll().single().notionalInvested)
    }

    @Test
    fun referentialIntegrityProtectsTypesAndInstances() {
        seedFullHierarchy()
        val typeId = productTypeRepository.findAll().single().id!!
        val productId = productRepository.findAll().single().id!!
        val orderId = orderRepository.findAll().single().id!!

        withPage("/blotter") { page ->
            page.expectBlotterActionProtected("product-type", typeId, "edit")
            page.expectBlotterActionProtected("product-type", typeId, "delete")
            page.expectBlotterActionProtected("product-instance", productId, "edit")
            page.expectBlotterActionProtected("product-instance", productId, "delete")
            page.expectBlotterActionEnabled("order", orderId, "edit")
            page.expectBlotterActionEnabled("order", orderId, "delete")
        }
    }

    @Test
    fun deleteOrderThenProductThenType() {
        seedFullHierarchy()
        val typeId = productTypeRepository.findAll().single().id!!
        val productId = productRepository.findAll().single().id!!
        val orderId = orderRepository.findAll().single().id!!

        withPage("/blotter") { page ->
            page.clickBlotterDelete("order", orderId)
            page.confirmDeleteDialog()
            assertEquals(0, orderRepository.count())

            assertThat(page.getByTestId("blotter-delete-product-instance-$productId")).isEnabled()
            page.clickBlotterDelete("product-instance", productId)
            page.confirmDeleteDialog()
            assertEquals(0, productRepository.count())

            assertThat(page.getByTestId("blotter-delete-product-type-$typeId")).isEnabled()
            page.clickBlotterDelete("product-type", typeId)
            page.confirmDeleteDialog()
            assertEquals(0, productTypeRepository.count())
        }

        assertTrue(productTypeRepository.findAll().isEmpty())
        assertTrue(productRepository.findAll().isEmpty())
        assertTrue(orderRepository.findAll().isEmpty())
    }
}
