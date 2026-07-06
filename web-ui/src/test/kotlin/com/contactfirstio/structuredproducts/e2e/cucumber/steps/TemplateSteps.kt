package com.contactfirstio.structuredproducts.e2e.cucumber.steps

import com.contactfirstio.structuredproducts.e2e.TemplateE2EFlows
import com.contactfirstio.structuredproducts.e2e.VaadinFormHelpers
import com.contactfirstio.structuredproducts.e2e.cucumber.PlaywrightScenarioContext
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class TemplateSteps(
    private val context: PlaywrightScenarioContext,
) {
    @Given("I am on the templates admin page")
    fun openTemplatesPage() {
        context.openPage("/admin/templates")
    }

    @When("I create a template with description {string}")
    fun createTemplate(description: String) {
        context.setDescription(description)
        val templateName = context.newTemplateName("E2E Template")
        TemplateE2EFlows.createTemplate(context.page, templateName, description)
    }

    @Given("I have created a template with description {string}")
    fun givenCreatedTemplate(description: String) {
        context.setDescription(description)
        val templateName = context.newTemplateName("E2E Template")
        TemplateE2EFlows.createTemplate(context.page, templateName, description)
    }

    @When("I open the editor for the current template")
    fun openEditorForCurrentTemplate() {
        val page = context.page
        val templateName = context.currentTemplateName
        page.locator("#template-detail-panel")
            .filter(Locator.FilterOptions().setHasText(templateName))
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Edit").setExact(true))
            .click()
        page.getByLabel("Template name").locator("input").waitFor()
        page.locator(".template-editor-view").waitFor()

        assertThat(page.getByLabel("Template name").locator("input")).hasValue(templateName)
        assertThat(page.getByLabel("Description").locator("input")).hasValue(context.currentDescription)
    }

    @When("I update the description to {string}")
    fun updateDescription(description: String) {
        context.setDescription(description)
        VaadinFormHelpers.fillTextField(context.page, "Description", description)
    }

    @When("I save the template")
    fun saveTemplate() {
        context.page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save")).click()
    }

    @When("I delete the current template from the list")
    fun deleteCurrentTemplate() {
        TemplateE2EFlows.deleteTemplateFromList(context.page, context.currentTemplateName)
    }

    @Then("the current template should appear in the list")
    fun assertCurrentTemplateInList() {
        val page = context.page
        val templateName = context.currentTemplateName
        assertThat(page.locator(".template-list-grid")).isVisible()
        assertThat(page.locator(".template-list-grid").getByText(templateName)).isVisible()
    }

    @Then("I should see the saved template confirmation")
    fun assertSavedConfirmation() {
        assertThat(context.page.getByText("Saved template: ${context.currentTemplateName}")).isVisible()
    }

    @Then("the template list should show the updated description")
    fun assertUpdatedDescriptionInList() {
        val page = context.page
        val templateName = context.currentTemplateName
        val description = context.currentDescription
        assertThat(page.locator(".template-list-grid")).isVisible()
        assertThat(page.locator(".template-list-grid").getByText(templateName)).isVisible()
        assertThat(page.locator(".template-list-grid").getByText(description)).isVisible()
    }

    @Then("the current template should no longer appear in the list")
    fun assertCurrentTemplateNotInList() {
        val page = context.page
        val templateName = context.currentTemplateName
        assertThat(
            page.locator("vaadin-grid.template-list-grid")
                .locator("[part='body-cell-content']")
                .filter(Locator.FilterOptions().setHasText(templateName)),
        ).hasCount(0)
    }
}
