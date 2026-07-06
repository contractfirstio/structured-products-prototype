package com.contactfirstio.structuredproducts.e2e.cucumber

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import io.cucumber.spring.ScenarioScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
@ScenarioScope
class PlaywrightScenarioContext @Autowired constructor(
    private val environment: Environment,
) {
    private var playwright: Playwright? = null
    private var browser: Browser? = null
    lateinit var page: Page

    var currentTemplateName: String = ""

    var currentDescription: String = ""

    fun baseUrl(): String {
        val port =
            environment.getProperty("local.server.port")
                ?: error("local.server.port is not available")
        return "http://localhost:$port"
    }

    fun openPage(path: String) {
        ensureBrowser()
        page.navigate("${baseUrl()}$path")
        page.waitForLoadState(LoadState.DOMCONTENTLOADED)
        page.locator("vaadin-app-layout").waitFor()
    }

    fun newTemplateName(prefix: String): String {
        currentTemplateName = "$prefix ${System.currentTimeMillis()}"
        return currentTemplateName
    }

    fun setDescription(description: String) {
        currentDescription = description
    }

    fun close() {
        if (::page.isInitialized) {
            page.close()
        }
        browser?.close()
        playwright?.close()
        browser = null
        playwright = null
    }

    private fun ensureBrowser() {
        if (playwright != null) {
            return
        }
        val headed = System.getenv().getOrDefault("E2E_HEADED", "false").toBoolean()
        playwright = Playwright.create()
        browser =
            playwright!!.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(!headed)
                    .setSlowMo(if (headed) 100.0 else 0.0),
            )
        page = browser!!.newPage()
        page.setDefaultTimeout(60_000.0)
    }
}
