package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class LegSchemaBuilderE2ETest : DatabaseCleanupE2ETest() {

    @Test
    fun legBuilderPageLoads() {
        withPage("/admin/leg-builder") { page ->
            assertThat(page.getByText("Leg Schema Builder").first()).isVisible()
            assertThat(page.getByLabel("Leg Name")).isVisible()
            assertThat(page.getByLabel("Field Name")).isVisible()
            assertThat(page.getByLabel("Data Type")).isVisible()
        }
    }

    @Test
    fun saveRequiresLegName() {
        withPage("/admin/leg-builder") { page ->
            page.saveLegSchema()
            page.waitForNotification("Leg name is required")
        }
        assertEquals(0, legSchemaRepository.count())
    }

    @Test
    fun saveRequiresAtLeastOneFieldDefinition() {
        withPage("/admin/leg-builder") { page ->
            page.fillLegName("Empty Fields Leg")
            page.fillFieldNameAt(0, "")
            page.saveLegSchema()
            page.waitForNotification("Add at least one field definition")
        }
        assertEquals(0, legSchemaRepository.count())
    }

    @Test
    fun savesProtectionLegSchemaToDatabase() {
        withPage("/admin/leg-builder") { page ->
            page.createProtectionLegSchema()
        }

        val saved = legSchemaRepository.findAll().single()
        assertEquals(PROTECTION_LEG_NAME, saved.name)
        assertEquals(1, saved.fields.size)
        assertEquals("Protection Level (%)", saved.fields.first().fieldName)
        assertEquals(true, saved.fields.first().isRequired)
    }

    @Test
    fun savesOptionalFieldOnUpsideLeg() {
        withPage("/admin/leg-builder") { page ->
            page.createUpsideLegSchema()
        }

        val saved = legSchemaRepository.findAll().single()
        assertEquals(UPSIDE_LEG_NAME, saved.name)
        assertEquals(false, saved.fields.first().isRequired)
    }

    @Test
    fun enumFieldRequiresOptions() {
        withPage("/admin/leg-builder") { page ->
            page.createEnumLegSchemaWithoutOptions()
            page.waitForNotification("ENUM field 'Option Style' requires at least one option")
        }
        assertEquals(0, legSchemaRepository.count())
    }

    @Test
    fun savesLegSchemaWithMultipleFieldTypes() {
        withPage("/admin/leg-builder") { page ->
            page.createFullFeatureLegSchema()
        }

        val saved = legSchemaRepository.findAll().single()
        assertEquals(FULL_FEATURE_LEG_NAME, saved.name)
        assertEquals(5, saved.fields.size)
        assertEquals(listOf("Notes", "Barrier Level", "Observation Count", "Autocallable", "Payoff Style"), saved.fields.map { it.fieldName })
    }

    @Test
    fun canAddAndRemoveFieldRows() {
        withPage("/admin/leg-builder") { page ->
            page.fillLegName("Removable Field Leg")
            page.addFieldDefinitionRow()
            assertThat(page.getByLabel("Field Name")).hasCount(2)

            page.locator("vaadin-button[theme~='error']").last().click()
            assertThat(page.getByLabel("Field Name")).hasCount(1)
        }
    }
}
