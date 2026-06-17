package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.service.FieldCatalogService
import com.contactfirstio.structuredproducts.service.TemplateAttachmentService
import com.vaadin.flow.component.Component as VaadinComponent
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datetimepicker.DateTimePicker
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Component
class TemplateDefaultFieldFactory(
    private val templateAttachmentService: TemplateAttachmentService,
    private val fieldCatalogService: FieldCatalogService,
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun createEditor(
        field: CatalogFieldDefinition,
        currentValue: String?,
        templateId: String?,
        onValueChange: (String?) -> Unit,
        formLayout: Boolean = false,
    ): VaadinComponent {
        if (field.systemManagedAtProductCreation) {
            return Span("Set by system").apply {
                addClassName("template-field-placeholder")
            }
        }
        if (!field.defaultedInTemplateCreation) {
            return Span("Set at product creation").apply {
                addClassName("template-field-placeholder")
            }
        }

        if (field.dataType == FieldDataType.FILE_LIST) {
            return TemplateAttachmentEditor(
                field = field,
                templateId = templateId,
                attachmentService = templateAttachmentService,
                currentValue = currentValue,
                onValueChange = onValueChange,
            )
        }

        return when {
            field.dataType == FieldDataType.CHECKBOX_GROUP ->
                CheckboxGroup<String>().apply {
                    addClassName("template-default-editor")
                    if (formLayout) addClassName("template-form-editor")
                    addClassName("template-checkbox-group")
                    setItems(field.enumOptions)
                    width = "100%"
                    value = fieldCatalogService.parseCheckboxGroupValue(currentValue)
                    addValueChangeListener { event ->
                        onValueChange(fieldCatalogService.serializeCheckboxGroupValue(event.value))
                    }
                }

            field.dataType == FieldDataType.BOOLEAN && field.enumOptions.isNotEmpty() ->
                RadioButtonGroup<String>().apply {
                    addClassName("template-default-editor")
                    if (formLayout) addClassName("template-form-editor")
                    addClassName("template-yes-no-radio")
                    setItems(field.enumOptions)
                    width = "100%"
                    value = currentValue?.takeIf { it.isNotBlank() }
                    addValueChangeListener { event ->
                        onValueChange(event.value?.takeIf { it.isNotBlank() })
                    }
                }

            field.enumOptions.isNotEmpty() ->
                ComboBox<String>().apply {
                    addClassName("template-default-editor")
                    if (formLayout) addClassName("template-form-editor")
                    placeholder = "Select..."
                    setItems(field.enumOptions)
                    isClearButtonVisible = true
                    width = "100%"
                    value = currentValue?.takeIf { it.isNotBlank() }
                    addValueChangeListener { event ->
                        onValueChange(event.value?.takeIf { it.isNotBlank() })
                    }
                }

            field.dataType == FieldDataType.DATE ->
                DatePicker().apply {
                    addClassName("template-default-editor")
                    if (formLayout) addClassName("template-form-editor")
                    width = "100%"
                    currentValue?.takeIf { it.isNotBlank() }?.let { parseDate(it) }?.also { value = it }
                    addValueChangeListener { event ->
                        onValueChange(event.value?.format(dateFormatter))
                    }
                }

            field.dataType == FieldDataType.DATETIME ->
                DateTimePicker().apply {
                    addClassName("template-default-editor")
                    if (formLayout) addClassName("template-form-editor")
                    width = "100%"
                    currentValue?.takeIf { it.isNotBlank() }?.let { parseDateTime(it) }?.also { value = it }
                    addValueChangeListener { event ->
                        onValueChange(event.value?.format(dateTimeFormatter))
                    }
                }

            field.dataType == FieldDataType.DOUBLE ->
                NumberField().apply {
                    addClassName("template-default-editor")
                    if (formLayout) addClassName("template-form-editor")
                    placeholder = "Value"
                    width = "100%"
                    step = 0.01
                    currentValue?.takeIf { it.isNotBlank() }?.toDoubleOrNull()?.let { value = it }
                    addValueChangeListener { event ->
                        onValueChange(
                            if (event.value == null || isEmpty) null else event.value.toString(),
                        )
                    }
                }

            field.key == "pay_off" ->
                TextArea().apply {
                    addClassName("template-default-editor")
                    addClassName("template-pay-off-editor")
                    if (formLayout) addClassName("template-form-editor")
                    placeholder = "Enter pay off description"
                    width = "100%"
                    minHeight = "10rem"
                    value = currentValue.orEmpty()
                    addValueChangeListener { event ->
                        onValueChange(event.value?.trim()?.takeIf { it.isNotEmpty() })
                    }
                }

            else ->
                TextField().apply {
                    addClassName("template-default-editor")
                    if (formLayout) addClassName("template-form-editor")
                    placeholder = "Value"
                    width = "100%"
                    value = currentValue.orEmpty()
                    addValueChangeListener { event ->
                        onValueChange(event.value?.trim()?.takeIf { it.isNotEmpty() })
                    }
                }
        }
    }

    private fun parseDate(value: String): LocalDate? =
        try {
            LocalDate.parse(value, dateFormatter)
        } catch (_: DateTimeParseException) {
            null
        }

    private fun parseDateTime(value: String): LocalDateTime? =
        try {
            LocalDateTime.parse(value, dateTimeFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
}
