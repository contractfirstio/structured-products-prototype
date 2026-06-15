package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.LoadState

const val PPN_TYPE_NAME = "Principal Protected Note"
const val PROTECTION_LEG_NAME = "Protection Leg"
const val UPSIDE_LEG_NAME = "Upside Leg"
const val FULL_FEATURE_LEG_NAME = "Full Feature Leg"
const val ALL_TYPES_PRODUCT_TYPE = "All Field Types Note"

fun Page.waitForAppReady() {
    waitForLoadState(LoadState.DOMCONTENTLOADED)
    locator("vaadin-app-layout").waitFor()
}

fun Page.navigateAndWait(baseUrl: String, path: String) {
    navigate("$baseUrl$path")
    waitForAppReady()
}

fun Page.clickButton(name: String) {
    getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(name)).click()
}

fun Page.selectComboBoxItem(label: String, itemText: String) {
    getByLabel(label).click()
    locator("vaadin-combo-box-item")
        .filter(Locator.FilterOptions().setHasText(itemText))
        .click()
}

fun Page.selectComboBoxItemAt(label: String, index: Int, itemText: String) {
    getByLabel(label).nth(index).click()
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

fun Page.featureCard(title: String): Locator =
    locator(".feature-card").filter(Locator.FilterOptions().setHasText(title))

// --- Leg Schema Builder ---

fun Page.fillLegName(name: String) {
    getByLabel("Leg Name").fill(name)
}

fun Page.fillFieldNameAt(index: Int, name: String) {
    getByLabel("Field Name").nth(index).fill(name)
}

fun Page.selectFieldDataTypeAt(index: Int, dataType: String) {
    selectComboBoxItemAt("Data Type", index, dataType)
}

fun Page.setFieldRequiredAt(index: Int, required: Boolean) {
    val checkbox = getByLabel("Required").nth(index)
    if (required) checkbox.check() else checkbox.uncheck()
}

fun Page.fillEnumOptionsAt(index: Int, options: String) {
    getByLabel("Enum Options (comma-separated)").nth(index).fill(options)
}

fun Page.addFieldDefinitionRow() {
    clickButton("Add Field")
}

fun Page.saveLegSchema() {
    clickButton("Save Leg Schema")
}

fun Page.createProtectionLegSchema() {
    fillLegName(PROTECTION_LEG_NAME)
    fillFieldNameAt(0, "Protection Level (%)")
    saveLegSchema()
    waitForNotification("Saved leg schema: $PROTECTION_LEG_NAME")
}

fun Page.createUpsideLegSchema() {
    fillLegName(UPSIDE_LEG_NAME)
    fillFieldNameAt(0, "Participation Rate (%)")
    setFieldRequiredAt(0, required = false)
    saveLegSchema()
    waitForNotification("Saved leg schema: $UPSIDE_LEG_NAME")
}

fun Page.createFullFeatureLegSchema() {
    fillLegName(FULL_FEATURE_LEG_NAME)

    fillFieldNameAt(0, "Notes")
    selectFieldDataTypeAt(0, "STRING")

    addFieldDefinitionRow()
    fillFieldNameAt(1, "Barrier Level")
    selectFieldDataTypeAt(1, "DOUBLE")

    addFieldDefinitionRow()
    fillFieldNameAt(2, "Observation Count")
    selectFieldDataTypeAt(2, "INTEGER")

    addFieldDefinitionRow()
    fillFieldNameAt(3, "Autocallable")
    selectFieldDataTypeAt(3, "BOOLEAN")

    addFieldDefinitionRow()
    fillFieldNameAt(4, "Payoff Style")
    selectFieldDataTypeAt(4, "ENUM")
    fillEnumOptionsAt(4, "European, American")
    setFieldRequiredAt(4, required = false)

    saveLegSchema()
    waitForNotification("Saved leg schema: $FULL_FEATURE_LEG_NAME")
}

fun Page.createEnumLegSchemaWithoutOptions() {
    fillLegName("Invalid Enum Leg")
    fillFieldNameAt(0, "Option Style")
    selectFieldDataTypeAt(0, "ENUM")
    saveLegSchema()
}

// --- Product Type ---

fun Page.createPpnProductType(typeName: String = PPN_TYPE_NAME) {
    getByLabel("Product Type Name").fill(typeName)
    getByLabel("Requires Underlying").check()
    getByLabel("Requires Maturity Date").check()

    clickButton("Add Leg Schema")
    selectComboBoxItemAt("Leg Schema", 0, PROTECTION_LEG_NAME)

    clickButton("Add Leg Schema")
    selectComboBoxItemAt("Leg Schema", 1, UPSIDE_LEG_NAME)

    clickButton("Save Product Type")
    waitForNotification("Saved product type: $typeName")
}

fun Page.createProductTypeWithSingleLeg(
    typeName: String,
    legSchemaName: String,
    requiresUnderlying: Boolean = true,
    requiresMaturity: Boolean = true,
) {
    getByLabel("Product Type Name").fill(typeName)
    if (requiresUnderlying) getByLabel("Requires Underlying").check() else getByLabel("Requires Underlying").uncheck()
    if (requiresMaturity) getByLabel("Requires Maturity Date").check() else getByLabel("Requires Maturity Date").uncheck()

    clickButton("Add Leg Schema")
    selectComboBoxItemAt("Leg Schema", 0, legSchemaName)

    clickButton("Save Product Type")
    waitForNotification("Saved product type: $typeName")
}

fun Page.saveProductType() {
    clickButton("Save Product Type")
}

// --- Product Instance ---

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

fun Page.fillFullFeatureProductForm(
    underlying: String,
    maturityMonths: String,
    notes: String,
    barrierLevel: String,
    observationCount: String,
    autocallable: Boolean = true,
    payoffStyle: String? = null,
) {
    getByLabel("Underlying").fill(underlying)
    getByLabel("Maturity (months)").fill(maturityMonths)
    getByLabel("Notes").fill(notes)
    getByLabel("Barrier Level").fill(barrierLevel)
    getByLabel("Observation Count").fill(observationCount)
    if (autocallable) {
        getByLabel("Autocallable").check()
    } else {
        getByLabel("Autocallable").uncheck()
    }
    if (payoffStyle != null) {
        selectComboBoxItem("Payoff Style", payoffStyle)
    }
}

fun Page.submitProductForm() {
    clickButton("Submit")
}

// --- Order Entry ---

fun Page.submitOrder(underlying: String, notional: String) {
    selectComboBoxItem("Active Product", underlying)
    getByLabel("Notional Amount").fill(notional)
    clickButton("Submit Order")
}

// --- Blotter ---

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
