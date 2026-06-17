package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class ProductTemplateE2ETest : BaseE2ETest() {

    @Test
    fun createProductTemplateFromList() {
        withPage("/admin/templates") { page ->
            assertThat(page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Product Templates"))).isVisible()
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Create template")).click()
            assertThat(page.getByLabel("Template name")).isVisible()

            page.getByLabel("Template name").fill("E2E PPN Template")
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save")).click()

            assertThat(page.getByText("Saved template: E2E PPN Template")).isVisible()
            assertThat(page.getByLabel("Template name")).hasValue("E2E PPN Template")
        }
    }
}
