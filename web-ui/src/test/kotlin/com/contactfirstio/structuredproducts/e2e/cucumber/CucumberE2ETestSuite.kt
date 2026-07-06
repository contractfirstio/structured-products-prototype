package com.contactfirstio.structuredproducts.e2e.cucumber

import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME
import org.junit.jupiter.api.Tag
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@Tag("e2e")
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.contactfirstio.structuredproducts.e2e.cucumber")
class CucumberE2ETestSuite
