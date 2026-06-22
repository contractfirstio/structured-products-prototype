package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StructuredProductsE2ETest : BaseE2ETest() {

    @Test
    @Order(1)
    fun createProductTemplateFromList() {
        val templateName = "E2E Template ${System.currentTimeMillis()}"

        withPage("/admin/templates") { page ->
            TemplateE2EFlows.createTemplate(page, templateName, "Created by end-to-end test")
        }
    }

    @Test
    @Order(2)
    fun editExistingProductTemplateFromList() {
        val templateName = "E2E Edit Template ${System.currentTimeMillis()}"
        val originalDescription = "Created by end-to-end test"
        val updatedDescription = "Updated by end-to-end test"

        withPage("/admin/templates") { page ->
            TemplateE2EFlows.createTemplate(page, templateName, originalDescription)

            openEditorForTemplate(page, templateName)

            assertThat(page.getByLabel("Template name").locator("input")).hasValue(templateName)
            assertThat(page.getByLabel("Description").locator("input")).hasValue(originalDescription)

            VaadinFormHelpers.fillTextField(page, "Description", updatedDescription)
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save")).click()

            assertThat(page.getByText("Saved template: $templateName")).isVisible()
            assertThat(page.locator(".template-list-grid")).isVisible()
            assertThat(page.locator(".template-list-grid").getByText(templateName)).isVisible()
            assertThat(page.locator(".template-list-grid").getByText(updatedDescription)).isVisible()
        }
    }

    private fun openEditorForTemplate(page: Page, templateName: String) {
        page.locator("#template-detail-panel")
            .filter(Locator.FilterOptions().setHasText(templateName))
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Edit").setExact(true))
            .click()
        page.getByLabel("Template name").locator("input").waitFor()
        page.locator(".template-editor-view").waitFor()
    }

    @Test
    @Order(3)
    fun deleteProductTemplateFromList() {
        val templateName = "E2E Delete Template ${System.currentTimeMillis()}"

        withPage("/admin/templates") { page ->
            TemplateE2EFlows.createTemplate(page, templateName, "Created by end-to-end test")
            TemplateE2EFlows.deleteTemplateFromList(page, templateName)
        }
    }

    @Test
    @Order(4)
    fun applicationServesVaadinUi() {
        withPage("/") { page ->
            assertThat(page.locator("vaadin-app-layout")).isVisible()
            assertThat(page.locator("vaadin-side-nav")).isVisible()
            assertThat(page.getByText("Demo Environment")).isVisible()
        }
    }
}
