package com.contactfirstio.structuredproducts

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.Theme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@Theme("structured-products")
class Application : AppShellConfigurator

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
