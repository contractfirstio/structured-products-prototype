package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.data.document.TemplateCustomFieldDefinition
import com.contactfirstio.structuredproducts.data.document.TemplateCustomFieldType
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode

class CustomFieldsPanel(
    private val customFields: MutableList<TemplateCustomFieldDefinition>,
) : VerticalLayout() {

    private val fieldsLayout =
        VerticalLayout().apply {
            addClassName("template-custom-fields-list")
            isPadding = false
            setWidthFull()
            setSpacing(true)
        }

    init {
        addClassName("template-custom-fields-panel")
        isPadding = false
        setWidthFull()
        setSpacing(true)

        add(
            H2("Custom fields").apply {
                addClassName("template-custom-fields-panel-title")
            },
            Paragraph(
                "Define additional fields for products created from this template. " +
                    "Choose a type and, for enum fields, list the allowed options separated by commas.",
            ).apply {
                addClassName("template-panel-hint")
            },
            fieldsLayout,
            Button("Add field", VaadinIcon.PLUS.create()).apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClassName("template-add-custom-field-button")
                addClickListener { addField() }
            },
        )

        if (customFields.isEmpty()) {
            addField()
        } else {
            rebuildFields()
        }
    }

    fun fieldCount(): Int = customFields.count { it.label.isNotBlank() }

    private fun addField() {
        val index = customFields.size
        customFields.add(
            TemplateCustomFieldDefinition(
                label = "",
                dataType = TemplateCustomFieldType.STRING,
            ),
        )
        fieldsLayout.add(fieldRow(index))
        focusLabel(index)
    }

    private fun focusLabel(index: Int) {
        fieldsLayout.children
            .skip(index.toLong())
            .findFirst()
            .ifPresent { row ->
                row.children
                    .flatMap { it.children }
                    .filter { it is TextField }
                    .findFirst()
                    .ifPresent { it.element.executeJs("this.focus()") }
            }
    }

    private fun fieldRow(index: Int): HorizontalLayout {
        val field = customFields[index]

        val labelField =
            TextField().apply {
                addClassName("template-form-editor")
                label = "Field name"
                placeholder = "e.g. Settlement delay"
                width = "100%"
                value = field.label
                valueChangeMode = ValueChangeMode.EAGER
                addValueChangeListener { event ->
                    customFields[index] = customFields[index].copy(label = event.value.orEmpty())
                }
            }

        val typeField =
            ComboBox<TemplateCustomFieldType>().apply {
                addClassName("template-custom-field-type")
                label = "Type"
                setItems(TemplateCustomFieldType.entries)
                setItemLabelGenerator { typeLabel(it) }
                value = field.dataType
                width = "10rem"
                addValueChangeListener { event ->
                    val selected = event.value ?: TemplateCustomFieldType.STRING
                    customFields[index] =
                        customFields[index].copy(
                            dataType = selected,
                            enumOptions =
                                if (selected == TemplateCustomFieldType.ENUM) {
                                    customFields[index].enumOptions
                                } else {
                                    emptyList()
                                },
                        )
                    rebuildFields()
                }
            }

        val enumOptionsField =
            TextField().apply {
                addClassName("template-form-editor")
                label = "Enum options"
                placeholder = "Option 1, Option 2, Option 3"
                width = "100%"
                value = field.enumOptions.joinToString(", ")
                isVisible = field.dataType == TemplateCustomFieldType.ENUM
                valueChangeMode = ValueChangeMode.EAGER
                addValueChangeListener { event ->
                    val options =
                        event.value.orEmpty()
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                    customFields[index] = customFields[index].copy(enumOptions = options)
                }
            }

        val mandatoryCheckbox =
            Checkbox("Mandatory").apply {
                value = field.mandatory
                addValueChangeListener { event ->
                    customFields[index] = customFields[index].copy(mandatory = event.value)
                }
            }

        val editorContent =
            VerticalLayout(
                HorizontalLayout(labelField, typeField).apply {
                    addClassName("template-custom-field-header")
                    setWidthFull()
                    isPadding = false
                    setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END)
                    expand(labelField)
                },
                enumOptionsField,
                HorizontalLayout(mandatoryCheckbox).apply {
                    addClassName("template-custom-field-footer")
                    isPadding = false
                },
            ).apply {
                isPadding = false
                setWidthFull()
                setSpacing(true)
            }

        return HorizontalLayout(
            editorContent,
            Button(VaadinIcon.TRASH.create()).apply {
                addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR)
                element.setAttribute("aria-label", "Remove custom field ${index + 1}")
                addClickListener {
                    if (customFields.size <= 1) {
                        customFields[0] =
                            TemplateCustomFieldDefinition(
                                label = "",
                                dataType = TemplateCustomFieldType.STRING,
                            )
                        rebuildFields()
                        return@addClickListener
                    }
                    customFields.removeAt(index)
                    rebuildFields()
                }
            },
        ).apply {
            addClassName("template-custom-field-row")
            setWidthFull()
            isPadding = false
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START)
            expand(editorContent)
        }
    }

    private fun rebuildFields() {
        fieldsLayout.removeAll()
        customFields.indices.forEach { index ->
            fieldsLayout.add(fieldRow(index))
        }
    }

    private fun typeLabel(type: TemplateCustomFieldType): String =
        when (type) {
            TemplateCustomFieldType.INTEGER -> "Integer"
            TemplateCustomFieldType.DOUBLE -> "Double"
            TemplateCustomFieldType.DATE -> "Date"
            TemplateCustomFieldType.STRING -> "String"
            TemplateCustomFieldType.ENUM -> "Enum"
        }
}
