package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Tag("e2e")
class NavigationE2ETest : BaseE2ETest() {

    @Test
    fun homePageShowsHeroAndFeatureCards() {
        withPage("/") { page ->
            assertThat(page.getByText("Structured products management")).isVisible()
            assertThat(page.locator(".feature-card").filter(com.microsoft.playwright.Locator.FilterOptions().setHasText("Define Product Types"))).isVisible()
            assertThat(page.locator(".feature-card").filter(com.microsoft.playwright.Locator.FilterOptions().setHasText("Create Products"))).isVisible()
            assertThat(page.locator(".feature-card").filter(com.microsoft.playwright.Locator.FilterOptions().setHasText("Execute Orders"))).isVisible()
            assertThat(page.locator(".feature-card").filter(com.microsoft.playwright.Locator.FilterOptions().setHasText("Data Blotter"))).isVisible()
        }
    }

    @ParameterizedTest
    @CsvSource(
        "/, Structured products management",
        "/blotter, Data Blotter",
        "/admin/create-type, Create Product Type",
        "/create-product, Create Product",
        "/order-entry, Order Entry",
    )
    fun directRoutesLoadExpectedContent(path: String, expectedText: String) {
        withPage(path) { page ->
            assertThat(page.getByText(expectedText).first()).isVisible()
        }
    }

    @Test
    fun sideNavigationVisitsAllPrimaryViews() {
        withPage("/") { page ->
            page.clickSideNavPath("blotter")
            assertThat(page.getByText("Data Blotter").first()).isVisible()
            assertThat(page.getByText("Product Types (Level 1)")).isVisible()

            page.clickSideNavPath("admin/create-type")
            assertThat(page.getByText("Create Product Type").first()).isVisible()

            page.clickSideNavPath("create-product")
            assertThat(page.getByText("Create Product").first()).isVisible()

            page.clickSideNavPath("order-entry")
            assertThat(page.getByText("Order Entry").first()).isVisible()

            page.clickSideNavPath("")
            assertThat(page.getByText("Structured products management")).isVisible()
        }
    }

    @Test
    fun featureCardsNavigateToWorkflowPages() {
        withPage("/") { page ->
            page.locator(".feature-card").filter(com.microsoft.playwright.Locator.FilterOptions().setHasText("Define Product Types")).click()
            page.waitForAppReady()
            assertThat(page).hasURL("${baseUrl()}/admin/create-type")

            page.navigateAndWait(baseUrl(), "/")
            page.locator(".feature-card").filter(com.microsoft.playwright.Locator.FilterOptions().setHasText("Execute Orders")).click()
            page.waitForAppReady()
            assertThat(page).hasURL("${baseUrl()}/order-entry")
        }
    }
}
