package com.contactfirstio.structuredproducts.e2e

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.function.Consumer

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class BaseE2ETest {

    companion object {
        @Container
        @JvmStatic
        val mongoDBContainer: MongoDBContainer = MongoDBContainer("mongo:7.0")

        @JvmStatic
        @DynamicPropertySource
        fun configureMongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.connectionString }
        }
    }

    @LocalServerPort
    protected var port: Int = 0

    protected fun baseUrl(): String = "http://localhost:$port"

    protected fun withPage(path: String, action: Consumer<Page>) {
        val headed = System.getenv().getOrDefault("E2E_HEADED", "false").toBoolean()
        Playwright.create().use { playwright ->
            playwright.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(!headed)
                    .setSlowMo(if (headed) 100.0 else 0.0),
            ).use { browser ->
                browser.newPage().use { page ->
                    page.navigate("${baseUrl()}$path")
                    page.waitForLoadState(LoadState.DOMCONTENTLOADED)
                    page.locator("vaadin-app-layout").waitFor()
                    action.accept(page)
                }
            }
        }
    }
}
