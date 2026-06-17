package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class TemplateFixedValuesForm(
    categories: List<String>,
    fields: List<CatalogFieldDefinition>,
    private val editorFactory: TemplateDefaultFieldFactory,
    private val defaults: MutableMap<String, String>,
    private val templateId: String?,
) : VerticalLayout() {

    init {
        addClassName("template-fixed-values-form")
        isPadding = false
        setWidthFull()
        setSpacing(true)

        val fieldsByCategory =
            categories.mapNotNull { category ->
                val categoryFields = fields.filter { it.category == category }
                if (categoryFields.isEmpty()) null else category to categoryFields
            }

        if (fieldsByCategory.isEmpty()) {
            add(
                Span("No fixed value fields match your search.").apply {
                    addClassName("template-empty-state")
                },
            )
        } else {
            fieldsByCategory.forEach { (category, categoryFields) ->
                add(
                    Div().apply {
                        addClassName("template-fixed-values-section")
                        add(H3(category).apply { addClassName("template-fixed-values-section-title") })
                        add(
                            Div().apply {
                                addClassName("template-fixed-values-grid")
                                categoryFields.forEach { field ->
                                    add(
                                        TemplateFormField.create(
                                            field = field,
                                            editorFactory = editorFactory,
                                            currentValue = defaults[field.key],
                                            templateId = templateId,
                                            onValueChange = { newValue ->
                                                if (newValue.isNullOrBlank()) {
                                                    defaults.remove(field.key)
                                                } else {
                                                    defaults[field.key] = newValue
                                                }
                                            },
                                        ),
                                    )
                                }
                            },
                        )
                    },
                )
            }
        }
    }
}
