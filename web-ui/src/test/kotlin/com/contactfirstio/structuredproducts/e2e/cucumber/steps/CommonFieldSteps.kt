package com.contactfirstio.structuredproducts.e2e.cucumber.steps

import com.contactfirstio.structuredproducts.e2e.CommonFieldE2EFlows
import com.contactfirstio.structuredproducts.e2e.TemplateE2EFlows
import com.contactfirstio.structuredproducts.e2e.cucumber.PlaywrightScenarioContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class CommonFieldSteps(
    private val context: PlaywrightScenarioContext,
) {
    @When("I open the common fields section")
    fun openCommonFieldsSection() {
        CommonFieldE2EFlows.openCommonFieldsSection(context.page)
    }

    @Then("I should see the common field {string} in category {string} with type {string}")
    fun assertCommonFieldInCatalogue(name: String, category: String, type: String) {
        CommonFieldE2EFlows.assertCommonFieldDetails(context.page, name, category, type)
    }

    @Then("the common field {string} should not be included")
    fun assertCommonFieldNotIncluded(name: String) {
        CommonFieldE2EFlows.assertCommonFieldIncluded(context.page, name, included = false)
    }

    @Then("I should see the common field {string}")
    fun assertCommonFieldVisible(name: String) {
        CommonFieldE2EFlows.assertCommonFieldVisible(context.page, name)
    }

    @Given("I have included the common field {string} on the current template")
    fun givenIncludedCommonField(name: String) {
        TemplateE2EFlows.openEditorForTemplate(context.page, context.currentTemplateName)
        CommonFieldE2EFlows.openCommonFieldsSection(context.page)
        CommonFieldE2EFlows.includeCommonField(context.page, name)
        context.page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save")).click()
        assertThat(context.page.getByText("Saved template: ${context.currentTemplateName}")).isVisible()
        assertThat(context.page.locator(".template-list-grid")).isVisible()
    }

    @Then("the common field {string} should be included")
    fun assertCommonFieldIncluded(name: String) {
        CommonFieldE2EFlows.assertCommonFieldIncluded(context.page, name, included = true)
    }
}
