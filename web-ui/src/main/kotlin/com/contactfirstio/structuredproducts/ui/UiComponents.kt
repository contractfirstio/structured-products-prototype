package com.contactfirstio.structuredproducts.ui

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

object UiComponents {

    fun pageShell(wide: Boolean = false): VerticalLayout =
        VerticalLayout().apply {
            addClassName("page-shell")
            if (wide) addClassName("page-shell-wide")
            setWidthFull()
            isPadding = false
            isSpacing = false
        }

    fun pageHeader(
        title: String,
        vararg actions: Component,
        eyebrow: String? = null,
        subtitle: String? = null,
    ): VerticalLayout =
        VerticalLayout().apply {
            addClassName("page-header")
            isPadding = false
            isSpacing = false

            if (eyebrow != null) {
                add(Span(eyebrow).apply { addClassName("page-eyebrow") })
            }

            val titleHeading = H2(title).apply { addClassName("page-title") }
            if (actions.isNotEmpty()) {
                add(
                    HorizontalLayout(titleHeading, HorizontalLayout(*actions).apply {
                        addClassName("sp-actions-row")
                        isPadding = false
                    }).apply {
                        addClassName("page-header-top")
                        isPadding = false
                        expand(titleHeading)
                    },
                )
            } else {
                add(titleHeading)
            }

            if (subtitle != null) {
                add(Paragraph(subtitle).apply { addClassName("page-subtitle") })
            }
        }

    fun glassPanel(vararg cssClasses: String): Div =
        Div().apply {
            addClassName("glass-panel")
            cssClasses.forEach { addClassName(it) }
        }

    fun heroPanel(title: String, subtitle: String): Div =
        Div().apply {
            addClassNames("glass-panel", "hero-panel")
            add(
                H1(title).apply { addClassName("hero-title") },
                Paragraph(subtitle).apply { addClassName("hero-subtitle") },
            )
        }

    fun primaryButton(text: String, action: () -> Unit): Button =
        Button(text) { action() }.apply {
            element.themeList.add("primary")
        }
}
