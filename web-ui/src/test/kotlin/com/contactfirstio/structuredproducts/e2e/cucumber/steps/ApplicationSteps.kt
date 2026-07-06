package com.contactfirstio.structuredproducts.e2e.cucumber.steps

import com.contactfirstio.structuredproducts.e2e.cucumber.PlaywrightScenarioContext
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then

class ApplicationSteps(
    private val context: PlaywrightScenarioContext,
) {
    @Given("I open the application home page")
    fun openHomePage() {
        context.openPage("/")
    }

    @Then("I should see the Vaadin app layout")
    fun assertAppLayoutVisible() {
        assertThat(context.page.locator("vaadin-app-layout")).isVisible()
    }

    @Then("I should see the side navigation")
    fun assertSideNavVisible() {
        assertThat(context.page.locator("vaadin-side-nav")).isVisible()
    }

    @Then("I should see {string}")
    fun assertTextVisible(text: String) {
        assertThat(context.page.getByText(text)).isVisible()
    }
}
