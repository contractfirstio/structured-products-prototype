package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

object VaadinFormHelpers {

    fun fillTextField(page: Page, label: String, value: String) {
        page.getByLabel(label).locator("input").fill(value)
    }

    fun fillTextArea(page: Page, label: String, value: String) {
        page.getByLabel(label).locator("textarea").fill(value)
    }

    fun fillFormItemTextField(page: Page, label: String, value: String) {
        formItem(page, label).locator("input").fill(value)
    }

    fun fillFormItemTextArea(page: Page, label: String, value: String) {
        formItem(page, label).locator("textarea").fill(value)
    }

    fun selectFormItemComboBoxOption(page: Page, label: String, option: String) {
        val input = formItem(page, label).locator("vaadin-combo-box input")
        input.click()
        input.fill(option)
        input.press("Enter")
    }

    private fun formItem(page: Page, label: String): Locator =
        page.locator("vaadin-form-item").filter(Locator.FilterOptions().setHasText(label))
}
