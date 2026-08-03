package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import kotlin.math.abs

object CommonFieldE2EFlows {

    fun openCommonFieldsSection(page: Page) {
        page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Common fields").setExact(true)).click()
        assertThat(commonFieldsGrid(page)).isVisible()
    }

    fun searchCommonFields(page: Page, query: String) {
        val panel = page.locator(".template-common-fields-panel")
        val category = panel.locator("vaadin-combo-box").first()
        category.click()
        category.locator("input").fill("Market & Technical")
        category.locator("input").press("Enter")

        val search = panel.locator("vaadin-text-field[placeholder='Search fields...']").locator("input")
        search.click()
        search.fill(query)
        search.dispatchEvent("input")
        search.press("Enter")

        fieldNameCell(page, query).first().waitFor()
    }

    fun includeCommonField(page: Page, displayName: String) {
        searchCommonFields(page, displayName)
        val checkbox = includeCheckboxForField(page, displayName)
        if (!isVaadinCheckboxChecked(checkbox)) {
            checkbox.click()
        }
        assertThat(checkbox).hasAttribute("checked", "")
    }

    fun assertCommonFieldVisible(page: Page, displayName: String) {
        searchCommonFields(page, displayName)
        assertThat(fieldNameCell(page, displayName).first()).isVisible()
    }

    fun assertCommonFieldDetails(
        page: Page,
        displayName: String,
        category: String,
        type: String,
    ) {
        searchCommonFields(page, displayName)
        val nameCell = fieldNameCell(page, displayName).first()
        assertThat(nameCell).isVisible()
        assertThat(nameCell).hasText(displayName)
        assertThat(commonFieldsGrid(page).getByText(category, Locator.GetByTextOptions().setExact(true)).first()).isVisible()
        assertThat(commonFieldsGrid(page).getByText(type, Locator.GetByTextOptions().setExact(true)).first()).isVisible()
    }

    fun assertCommonFieldIncluded(page: Page, displayName: String, included: Boolean) {
        searchCommonFields(page, displayName)
        val checkbox = includeCheckboxForField(page, displayName)
        if (included) {
            assertThat(checkbox).hasAttribute("checked", "")
        } else {
            assertThat(checkbox).not().hasAttribute("checked", "")
        }
    }

    private fun includeCheckboxForField(page: Page, displayName: String): Locator {
        val nameBox = fieldNameCell(page, displayName).first().boundingBox()
            ?: error("No bounding box for common field '$displayName'")

        val checkboxes = commonFieldsGrid(page).locator("vaadin-checkbox")
        val count = checkboxes.count()
        for (i in 0 until count) {
            val checkbox = checkboxes.nth(i)
            val box = checkbox.boundingBox() ?: continue
            if (abs(box.y - nameBox.y) < 8.0) {
                return checkbox
            }
        }
        error("No include checkbox aligned with common field '$displayName'")
    }

    private fun isVaadinCheckboxChecked(checkbox: Locator): Boolean =
        checkbox.getAttribute("checked") != null

    private fun fieldNameCell(page: Page, displayName: String): Locator =
        commonFieldsGrid(page).getByText(displayName, Locator.GetByTextOptions().setExact(true))

    private fun commonFieldsGrid(page: Page): Locator =
        page.locator(".template-common-fields-panel vaadin-grid.template-common-grid")
}
