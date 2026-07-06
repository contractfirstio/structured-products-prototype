package com.contactfirstio.structuredproducts.e2e.cucumber.hooks

import com.contactfirstio.structuredproducts.e2e.cucumber.PlaywrightScenarioContext
import io.cucumber.java.After
import io.cucumber.java.Before

class PlaywrightHooks(
    private val context: PlaywrightScenarioContext,
) {
    @Before(order = 0)
    fun resetScenarioState() {
        context.newTemplateName("E2E Template")
        context.setDescription("")
    }

    @After
    fun closeBrowser() {
        context.close()
    }
}
