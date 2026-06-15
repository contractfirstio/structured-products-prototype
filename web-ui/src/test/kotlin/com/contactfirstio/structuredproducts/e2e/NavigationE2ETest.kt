package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Tag("e2e")
class NavigationE2ETest : BaseE2ETest() {

    @Test
    fun homePageShowsHeroAndAllFeatureCards() {
        withPage("/") { page ->
            assertThat(page.getByText("Structured products management")).isVisible()
            assertThat(page.featureCard("Define Leg Schemas")).isVisible()
            assertThat(page.featureCard("Define Product Types")).isVisible()
            assertThat(page.featureCard("Create Products")).isVisible()
            assertThat(page.featureCard("Execute Orders")).isVisible()
            assertThat(page.featureCard("Data Blotter")).isVisible()
        }
    }

    @ParameterizedTest
    @CsvSource(
        "/, Structured products management",
        "/blotter, Data Blotter",
        "/admin/leg-builder, Leg Schema Builder",
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
            assertThat(page.getByText("Leg Schemas (Level 1 · Admin)")).isVisible()
            assertThat(page.getByText("Product Types (Level 1)")).isVisible()

            page.clickSideNavPath("admin/leg-builder")
            assertThat(page.getByText("Leg Schema Builder").first()).isVisible()

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
            page.featureCard("Define Leg Schemas").click()
            page.waitForAppReady()
            assertThat(page).hasURL("${baseUrl()}/admin/leg-builder")

            page.navigateAndWait(baseUrl(), "/")
            page.featureCard("Define Product Types").click()
            page.waitForAppReady()
            assertThat(page).hasURL("${baseUrl()}/admin/create-type")

            page.navigateAndWait(baseUrl(), "/")
            page.featureCard("Create Products").click()
            page.waitForAppReady()
            assertThat(page).hasURL("${baseUrl()}/create-product")

            page.navigateAndWait(baseUrl(), "/")
            page.featureCard("Execute Orders").click()
            page.waitForAppReady()
            assertThat(page).hasURL("${baseUrl()}/order-entry")

            page.navigateAndWait(baseUrl(), "/")
            page.featureCard("Data Blotter").click()
            page.waitForAppReady()
            assertThat(page).hasURL("${baseUrl()}/blotter")
        }
    }
}
