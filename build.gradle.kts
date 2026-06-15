plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.vaadin) apply false
}

allprojects {
    group = "com.contactfirstio.structuredproducts"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://maven.vaadin.com/vaadin-addons")
    }
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            jvmToolchain(21)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
