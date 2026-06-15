pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.vaadin.com/vaadin-addons")
    }
}

rootProject.name = "structured-products-prototype"

include("data-access")
include("business-logic")
include("web-ui")
