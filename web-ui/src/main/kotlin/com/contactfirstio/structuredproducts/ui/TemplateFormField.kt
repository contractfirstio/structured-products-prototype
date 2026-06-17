package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.data.document.FieldRequirement
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

object TemplateFormField {

    fun create(
        field: CatalogFieldDefinition,
        editorFactory: TemplateDefaultFieldFactory,
        currentValue: String?,
        templateId: String?,
        onValueChange: (String?) -> Unit,
    ): Div =
        Div().apply {
            addClassName("template-form-field")
            val labelRow =
                HorizontalLayout().apply {
                    addClassName("template-form-field-header")
                    setWidthFull()
                    isPadding = false
                    setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
                    add(
                        Span(field.displayName).apply {
                            addClassName("template-form-field-label")
                        },
                    )
                    if (field.requirement == FieldRequirement.MANDATORY) {
                        add(
                            Span("Required").apply {
                                addClassName("template-field-badge")
                                element.setAttribute("data-requirement", "mandatory")
                            },
                        )
                    }
                }
            val editor =
                editorFactory.createEditor(
                    field = field,
                    currentValue = currentValue,
                    templateId = templateId,
                    onValueChange = onValueChange,
                    formLayout = true,
                )
            add(labelRow, editor)
        }
}
