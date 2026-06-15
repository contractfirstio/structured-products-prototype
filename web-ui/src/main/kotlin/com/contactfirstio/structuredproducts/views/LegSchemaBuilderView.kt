package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.service.CreateLegSchemaCommand
import com.contactfirstio.structuredproducts.service.FieldDefinitionDto
import com.contactfirstio.structuredproducts.service.LegSchemaService
import com.contactfirstio.structuredproducts.service.ValidationException
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility

@Route(value = "admin/leg-builder", layout = MainLayout::class)
class LegSchemaBuilderView(
    private val legSchemaService: LegSchemaService,
) : VerticalLayout() {

    private data class FieldRow(
        val layout: HorizontalLayout,
        val fieldNameField: TextField,
        val dataTypeField: ComboBox<FieldDataType>,
        val requiredField: Checkbox,
        val enumOptionsField: TextField,
    )

    private val nameField = TextField("Leg Name").apply {
        isRequired = true
        placeholder = "e.g. Asian Option Leg"
        width = "100%"
    }
    private val fieldRowsLayout = VerticalLayout().apply {
        addClassNames(LumoUtility.Gap.SMALL)
        isPadding = false
    }
    private val fieldRows = mutableListOf<FieldRow>()

    init {
        val shell = com.contactfirstio.structuredproducts.ui.UiComponents.pageShell()
        val panel = com.contactfirstio.structuredproducts.ui.UiComponents.glassPanel("form-panel")

        panel.add(
            FormLayout().apply {
                setResponsiveSteps(FormLayout.ResponsiveStep("0", 1))
                add(nameField)
            },
            H3("Field Definitions"),
            fieldRowsLayout,
            HorizontalLayout(
                Button("Add Field", VaadinIcon.PLUS.create()) { addFieldRow() },
                Button("Save Leg Schema") { save() }.apply {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                },
            ).apply { addClassNames(LumoUtility.Gap.SMALL) },
        )
        shell.add(
            com.contactfirstio.structuredproducts.ui.UiComponents.pageHeader(
                eyebrow = "Level 1 · Admin",
                title = "Leg Schema Builder",
                subtitle = "Define the fields required for a leg. Saved schemas can be linked to product types.",
            ),
            panel,
        )
        add(shell)
        setWidthFull()
        isPadding = false

        addFieldRow("Barrier Level", FieldDataType.DOUBLE, true)
    }

    private fun addFieldRow(
        defaultName: String? = null,
        defaultType: FieldDataType = FieldDataType.STRING,
        defaultRequired: Boolean = true,
    ) {
        val fieldNameField = TextField("Field Name").apply {
            isRequired = true
            width = "100%"
            value = defaultName.orEmpty()
        }
        val dataTypeField = ComboBox<FieldDataType>("Data Type").apply {
            setItems(FieldDataType.entries)
            isRequired = true
            value = defaultType
            width = "100%"
        }
        val requiredField = Checkbox("Required").apply { value = defaultRequired }
        val enumOptionsField = TextField("Enum Options (comma-separated)").apply {
            placeholder = "e.g. Call, Put"
            width = "100%"
            isVisible = defaultType == FieldDataType.ENUM
        }

        dataTypeField.addValueChangeListener { event ->
            enumOptionsField.isVisible = event.value == FieldDataType.ENUM
        }

        val rowLayout = HorizontalLayout().apply {
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END)
            width = "100%"
            addClassNames(LumoUtility.Gap.MEDIUM)
        }

        val fieldRow = FieldRow(rowLayout, fieldNameField, dataTypeField, requiredField, enumOptionsField)
        val removeButton = Button(VaadinIcon.TRASH.create()) {
            fieldRowsLayout.remove(rowLayout)
            fieldRows.remove(fieldRow)
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY)
        }

        rowLayout.add(fieldNameField, dataTypeField, requiredField, enumOptionsField, removeButton)
        rowLayout.expand(fieldNameField)
        fieldRows += fieldRow
        fieldRowsLayout.add(rowLayout)
    }

    private fun save() {
        if (nameField.isEmpty) {
            nameField.isInvalid = true
            Notification.show("Leg name is required", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            return
        }
        nameField.isInvalid = false

        val fields = fieldRows.mapNotNull { row ->
            val fieldName = row.fieldNameField.value?.trim().orEmpty()
            val dataType = row.dataTypeField.value ?: return@mapNotNull null
            if (fieldName.isEmpty()) return@mapNotNull null

            val enumOptions = if (dataType == FieldDataType.ENUM) {
                row.enumOptionsField.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }

            FieldDefinitionDto(
                fieldName = fieldName,
                dataType = dataType,
                isRequired = row.requiredField.value,
                enumOptions = enumOptions,
            )
        }

        if (fields.isEmpty()) {
            Notification.show("Add at least one field definition", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            return
        }

        try {
            val saved = legSchemaService.save(
                CreateLegSchemaCommand(
                    name = nameField.value,
                    fields = fields,
                ),
            )
            Notification.show("Saved leg schema: ${saved.name}", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            clearForm()
        } catch (ex: ValidationException) {
            Notification.show(ex.message ?: "Validation failed", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
        }
    }

    private fun clearForm() {
        nameField.clear()
        fieldRowsLayout.removeAll()
        fieldRows.clear()
        addFieldRow("Barrier Level", FieldDataType.DOUBLE, true)
    }
}
