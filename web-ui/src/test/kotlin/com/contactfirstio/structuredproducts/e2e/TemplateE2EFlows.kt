package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.WaitForSelectorState

object TemplateE2EFlows {

    fun createTemplate(page: Page, templateName: String, description: String) {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Create template")).click()
        page.locator(".template-editor-view").waitFor()

        VaadinFormHelpers.fillTextField(page, "Template name", templateName)
        VaadinFormHelpers.fillTextField(page, "Description", description)
        fillRequiredStandardFields(page)

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save")).click()
        assertThat(page.getByText("Created template: $templateName")).isVisible()
        assertThat(page.locator(".template-list-grid")).isVisible()
        assertThat(page.locator(".template-list-grid").getByText(templateName)).isVisible()
    }

    fun deleteTemplateFromList(page: Page, templateName: String) {
        page.locator(".template-list-grid")
            .filter(Locator.FilterOptions().setHasText(templateName))
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Delete $templateName"))
            .click()

        page.getByRole(AriaRole.ALERTDIALOG, Page.GetByRoleOptions().setName("Delete template?"))
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Delete").setExact(true))
            .click()

        assertThat(page.getByText("Deleted template: $templateName")).isVisible()

        val nameCell =
            page.locator("vaadin-grid.template-list-grid")
                .locator("[part='body-cell-content']")
                .filter(Locator.FilterOptions().setHasText(templateName))
        nameCell.first().waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED).setTimeout(15_000.0),
        )

        page.reload()
        page.waitForLoadState(LoadState.DOMCONTENTLOADED)
        page.locator("vaadin-app-layout").waitFor()
        assertThat(
            page.locator("vaadin-grid.template-list-grid")
                .locator("[part='body-cell-content']")
                .filter(Locator.FilterOptions().setHasText(templateName)),
        ).hasCount(0)
    }

    fun fillRequiredStandardFields(page: Page) {
        VaadinFormHelpers.selectFormItemComboBoxOption(page, "Risk Profile *", "Protection")
        VaadinFormHelpers.fillFormItemTextField(page, "PIP ID *", "PIP-E2E-001")
        VaadinFormHelpers.fillFormItemTextArea(page, "Pay Off *", "Capital protected at maturity")
        VaadinFormHelpers.fillFormItemTextField(page, "APC Code *", "APC-E2E-001")
    }
}
