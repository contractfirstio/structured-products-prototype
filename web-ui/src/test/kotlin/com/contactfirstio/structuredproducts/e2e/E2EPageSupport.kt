package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.LoadState

const val PPN_TYPE_NAME = "Principal Protected Note"

fun Page.waitForAppReady() {
    waitForLoadState(LoadState.DOMCONTENTLOADED)
    locator("vaadin-app-layout").waitFor()
}

fun Page.navigateAndWait(baseUrl: String, path: String) {
    navigate("$baseUrl$path")
    waitForAppReady()
}

fun Page.selectComboBoxItem(label: String, itemText: String) {
    getByLabel(label).click()
    locator("vaadin-combo-box-item")
        .filter(Locator.FilterOptions().setHasText(itemText))
        .click()
}

fun Page.waitForNotification(text: String) {
    locator("vaadin-notification-card").filter(Locator.FilterOptions().setHasText(text)).last().waitFor()
}

fun Page.waitForDialogTitle(title: String) {
    getByText(title).waitFor()
}

fun Page.fillDialogField(label: String, value: String) {
    getByRole(AriaRole.DIALOG).getByLabel(label).fill(value)
}

fun Page.clickSideNav(linkText: String) {
    locator("vaadin-side-nav-item")
        .filter(Locator.FilterOptions().setHasText(linkText))
        .last()
        .click()
    waitForAppReady()
}

fun Page.clickSideNavPath(path: String) {
    locator("vaadin-side-nav-item[path='$path']").click()
    waitForAppReady()
}

fun Page.createPpnProductType(typeName: String = PPN_TYPE_NAME) {
    getByLabel("Product Type Name").fill(typeName)
    getByLabel("Requires Underlying").check()
    getByLabel("Requires Maturity Date").check()
    getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save Product Type")).click()
    waitForNotification("Saved product type: $typeName")
}

fun Page.selectProductType(typeName: String) {
    selectComboBoxItem("Product Type", typeName)
}

fun Page.fillPpnProductForm(
    underlying: String,
    maturityMonths: String,
    protectionLevel: String,
    participationRate: String? = null,
) {
    getByLabel("Underlying").fill(underlying)
    getByLabel("Maturity (months)").fill(maturityMonths)
    getByLabel("Protection Level (%)").fill(protectionLevel)
    if (participationRate != null) {
        getByLabel("Participation Rate (%)").fill(participationRate)
    }
}

fun Page.submitProductForm() {
    getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
}

fun Page.submitOrder(underlying: String, notional: String) {
    selectComboBoxItem("Active Product", underlying)
    getByLabel("Notional Amount").fill(notional)
    getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit Order")).click()
}

fun Page.blotterSection(sectionTitle: String): Locator =
    locator(".blotter-section").filter(Locator.FilterOptions().setHasText(sectionTitle))

fun Locator.sectionGrid(): Locator = locator("vaadin-grid")

fun Locator.gridCellWith(text: String): Locator =
    sectionGrid().getByText(text, Locator.GetByTextOptions().setExact(false))

fun Page.clickBlotterEdit(entity: String, recordId: String) {
    getByTestId("blotter-edit-$entity-$recordId").click()
}

fun Page.clickBlotterDelete(entity: String, recordId: String) {
    getByTestId("blotter-delete-$entity-$recordId").click()
}

fun Page.expectBlotterActionProtected(entity: String, recordId: String, action: String) {
    val testId = if (action == "edit") "blotter-edit-$entity-$recordId" else "blotter-delete-$entity-$recordId"
    com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(getByTestId(testId))
        .hasAttribute("title", "Cannot $action while dependent records exist")
}

fun Page.expectBlotterActionEnabled(entity: String, recordId: String, action: String) {
    val testId = if (action == "edit") "blotter-edit-$entity-$recordId" else "blotter-delete-$entity-$recordId"
    com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(getByTestId(testId)).isEnabled()
}

fun Page.confirmDeleteDialog() {
    val notifications = locator("vaadin-notification-card")
    val existingCount = notifications.count()
    locator("vaadin-confirm-dialog")
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Delete"))
        .click()
    notifications.nth(existingCount).waitFor()
}

fun Page.saveDialog() {
    getByRole(AriaRole.DIALOG)
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Save"))
        .click()
}
