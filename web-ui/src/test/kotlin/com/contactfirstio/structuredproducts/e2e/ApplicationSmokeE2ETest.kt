package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("e2e")
class ApplicationSmokeE2ETest : BaseE2ETest() {

    @Test
    fun applicationStartsAndServesVaadinShell() {
        withPage("/") { page ->
            assertThat(page.locator("vaadin-app-layout")).isVisible()
            assertThat(page.getByText("Structured Products Platform")).isVisible()
            assertThat(page.getByText("Structured products management")).isVisible()
        }
    }
}
