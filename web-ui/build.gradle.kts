plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.vaadin)
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:${libs.versions.vaadin.get()}")
    }
}

dependencies {
    implementation(project(":business-logic"))
    implementation(libs.vaadin.spring.boot.starter)

    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.vaadin.dev)

    testImplementation(project(":data-access"))
    testImplementation(libs.spring.boot.starter.data.mongodb)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.playwright)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.spring.boot.testcontainers)
    testRuntimeOnly(libs.vaadin.dev)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("vaadinPrepareFrontend")
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("e2e")
    }
}

tasks.register<JavaExec>("installPlaywrightBrowsers") {
    description = "Installs Playwright browser binaries (and OS deps on Linux CI)"
    group = "verification"
    dependsOn("testClasses")
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.microsoft.playwright.CLI")
    args("install", "--with-deps", "chromium")
}

tasks.register<Test>("e2eTest") {
    description = "Runs browser end-to-end tests"
    group = "verification"
    dependsOn("testClasses", "installPlaywrightBrowsers")
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    maxParallelForks = 1
    useJUnitPlatform {
        includeTags("e2e")
    }
    outputs.upToDateWhen { false }
    outputs.cacheIf { false }
    testLogging {
        events("passed", "failed", "skipped")
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    shouldRunAfter(tasks.test)
}
