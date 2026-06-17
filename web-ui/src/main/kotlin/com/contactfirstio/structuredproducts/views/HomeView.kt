package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.ui.UiComponents
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLink

@Route(value = "", layout = MainLayout::class)
class HomeView : VerticalLayout() {

    init {
        val shell = UiComponents.pageShell()
        shell.add(
            UiComponents.heroPanel(
                title = "Thebes",
                subtitle = "Product Template → Product → Order. Configure templates, then capture product data.",
            ),
            Div().apply {
                addClassName("feature-grid")
                add(
                    RouterLink("Product Templates", TemplateListView::class.java).apply {
                        addClassName("feature-card")
                        add(
                            Span("Product Templates").apply { addClassName("feature-card-title") },
                            Span("Define fixed values and select common fields per template.")
                                .apply { addClassName("feature-card-text") },
                        )
                    },
                )
            },
        )
        add(shell)
        setWidthFull()
        isPadding = false
    }
}
