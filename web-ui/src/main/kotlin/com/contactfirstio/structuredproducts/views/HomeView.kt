package com.contactfirstio.structuredproducts.views

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLink

@Route(value = "", layout = MainLayout::class)
class HomeView : VerticalLayout() {

    init {
        val shell = com.contactfirstio.structuredproducts.ui.UiComponents.pageShell()
        shell.add(
            com.contactfirstio.structuredproducts.ui.UiComponents.heroPanel(
                title = "Structured products management",
                subtitle = "Configure product types, create instances, manage RFQs and orders, and maintain referential integrity across the full product hierarchy.",
            ),
            createFeatureGrid(),
        )
        add(shell)
        setWidthFull()
        isPadding = false
    }

    private fun createFeatureGrid(): Div =
        Div().apply {
            addClassName("feature-grid")
            add(
                featureLink(
                    LegSchemaBuilderView::class.java,
                    "Define Leg Schemas",
                    "Model Level 1 leg field definitions for dynamic data capture.",
                ),
                featureLink(
                    CreateProductTypeView::class.java,
                    "Define Product Types",
                    "Link leg schemas and global terms into product type templates.",
                ),
                featureLink(
                    CreateProductView::class.java,
                    "Create Products",
                    "Spawn Level 2 instances from dynamic type metadata.",
                ),
                featureLink(
                    OrderEntryView::class.java,
                    "Execute Orders",
                    "Trade ACTIVE products with validated notional entry.",
                ),
                featureLink(
                    BlotterView::class.java,
                    "Data Blotter",
                    "Inspect, edit, and delete the full object hierarchy.",
                ),
            )
        }

    private fun featureLink(
        target: Class<out com.vaadin.flow.component.Component>,
        title: String,
        text: String,
    ): RouterLink =
        RouterLink("", target).apply {
            addClassName("feature-card")
            add(
                Span(title).apply { addClassName("feature-card-title") },
                Span(text).apply { addClassName("feature-card-text") },
            )
            element.setAttribute("title", title)
        }
}
