package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class ProductTypeE2ETest : DatabaseCleanupE2ETest() {

    @Test
    fun createTypePageLoads() {
        withPage("/admin/create-type") { page ->
            assertThat(page.getByText("Create Product Type").first()).isVisible()
            assertThat(page.getByLabel("Product Type Name")).isVisible()
            assertThat(page.getByText("Allowed Leg Schemas")).isVisible()
        }
    }

    @Test
    fun saveRequiresProductTypeName() {
        withPage("/admin/create-type") { page ->
            page.saveProductType()
            page.waitForNotification("Product type name is required")
        }
        assertEquals(0, productTypeRepository.count())
    }

    @Test
    fun saveRequiresLinkedLegSchemas() {
        withPage("/admin/leg-builder") { it.createProtectionLegSchema() }

        withPage("/admin/create-type") { page ->
            page.getByLabel("Product Type Name").fill("Unlinked Type")
            page.saveProductType()
            page.waitForNotification("Add at least one allowed leg schema")
        }
        assertEquals(0, productTypeRepository.count())
    }

    @Test
    fun linksLegSchemasAndPersistsToDatabase() {
        seedPpnProductType()

        val saved = productTypeRepository.findAll().single()
        assertEquals(PPN_TYPE_NAME, saved.name)
        assertEquals(true, saved.globalTermsSchema.requiresUnderlying)
        assertEquals(true, saved.globalTermsSchema.requiresMaturityDate)
        assertEquals(2, saved.allowedLegSchemaIds.size)
        assertEquals(2, legSchemaRepository.count())
    }

    @Test
    fun respectsGlobalTermsToggles() {
        withPage("/admin/leg-builder") { it.createProtectionLegSchema() }

        withPage("/admin/create-type") { page ->
            page.createProductTypeWithSingleLeg(
                typeName = "Minimal Terms Note",
                legSchemaName = PROTECTION_LEG_NAME,
                requiresUnderlying = false,
                requiresMaturity = false,
            )
        }

        val saved = productTypeRepository.findAll().single()
        assertEquals(false, saved.globalTermsSchema.requiresUnderlying)
        assertEquals(false, saved.globalTermsSchema.requiresMaturityDate)
    }

    @Test
    fun productFormOmitsGlobalTermsWhenNotRequired() {
        withPage("/admin/leg-builder") { it.createProtectionLegSchema() }

        withPage("/admin/create-type") { page ->
            page.createProductTypeWithSingleLeg(
                typeName = "Minimal Terms Note",
                legSchemaName = PROTECTION_LEG_NAME,
                requiresUnderlying = false,
                requiresMaturity = false,
            )
        }

        withPage("/create-product") { page ->
            page.selectProductType("Minimal Terms Note")
            assertThat(page.getByLabel("Underlying")).hasCount(0)
            assertThat(page.getByLabel("Maturity (months)")).hasCount(0)
            assertThat(page.getByLabel("Protection Level (%)")).isVisible()
        }
    }
}
