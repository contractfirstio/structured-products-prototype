package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.service.CreateProductTypeCommand
import com.contactfirstio.structuredproducts.service.GlobalTermsSchemaDto
import com.contactfirstio.structuredproducts.service.LegSchemaDetail
import com.contactfirstio.structuredproducts.service.LegSchemaService
import com.contactfirstio.structuredproducts.service.ProductTypeService
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

@Route(value = "admin/create-type", layout = MainLayout::class)
class CreateProductTypeView(
    private val productTypeService: ProductTypeService,
    private val legSchemaService: LegSchemaService,
) : VerticalLayout() {

    private data class LegRow(
        val layout: HorizontalLayout,
        val legSchemaField: ComboBox<LegSchemaDetail>,
    )

    private val nameField = TextField("Product Type Name").apply {
        isRequired = true
        width = "100%"
    }
    private val requiresUnderlyingField = Checkbox("Requires Underlying").apply { value = true }
    private val requiresMaturityField = Checkbox("Requires Maturity Date").apply { value = true }
    private val legRowsLayout = VerticalLayout().apply {
        addClassNames(LumoUtility.Gap.SMALL)
        isPadding = false
    }
    private val legRows = mutableListOf<LegRow>()

    init {
        val shell = com.contactfirstio.structuredproducts.ui.UiComponents.pageShell()
        val panel = com.contactfirstio.structuredproducts.ui.UiComponents.glassPanel("form-panel")

        panel.add(
            FormLayout().apply {
                setResponsiveSteps(FormLayout.ResponsiveStep("0", 1))
                add(nameField, requiresUnderlyingField, requiresMaturityField)
            },
            H3("Allowed Leg Schemas"),
            legRowsLayout,
            HorizontalLayout(
                Button("Add Leg Schema", VaadinIcon.PLUS.create()) { addLegRow() },
                Button("Save Product Type") { save() }.apply {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                },
            ).apply { addClassNames(LumoUtility.Gap.SMALL) },
        )
        shell.add(
            com.contactfirstio.structuredproducts.ui.UiComponents.pageHeader(
                eyebrow = "Level 1 · Admin",
                title = "Create Product Type",
                subtitle = "Define global terms and link saved leg schemas. Saved types drive every downstream product form.",
            ),
            panel,
        )
        add(shell)
        setWidthFull()
        isPadding = false
    }

    private fun addLegRow(defaultLegSchema: LegSchemaDetail? = null) {
        val options = legSchemaService.findAll()
        val legSchemaField = ComboBox<LegSchemaDetail>("Leg Schema").apply {
            setItems(options)
            setItemLabelGenerator { it.name }
            isRequired = true
            width = "100%"
            value = defaultLegSchema ?: options.firstOrNull()
        }

        val rowLayout = HorizontalLayout().apply {
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END)
            width = "100%"
            addClassNames(LumoUtility.Gap.MEDIUM)
        }

        val legRow = LegRow(rowLayout, legSchemaField)
        val removeButton = Button(VaadinIcon.TRASH.create()) {
            legRowsLayout.remove(rowLayout)
            legRows.remove(legRow)
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY)
        }

        rowLayout.add(legSchemaField, removeButton)
        rowLayout.expand(legSchemaField)
        legRows += legRow
        legRowsLayout.add(rowLayout)
    }

    private fun save() {
        if (nameField.isEmpty) {
            nameField.isInvalid = true
            Notification.show("Product type name is required", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            return
        }
        nameField.isInvalid = false

        val allowedLegSchemaIds = legRows.mapNotNull { row ->
            row.legSchemaField.value?.id
        }.distinct()

        if (allowedLegSchemaIds.isEmpty()) {
            Notification.show("Add at least one allowed leg schema", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            return
        }

        try {
            val saved = productTypeService.save(
                CreateProductTypeCommand(
                    name = nameField.value,
                    globalTermsSchema = GlobalTermsSchemaDto(
                        requiresUnderlying = requiresUnderlyingField.value,
                        requiresMaturityDate = requiresMaturityField.value,
                    ),
                    allowedLegSchemaIds = allowedLegSchemaIds,
                ),
            )
            Notification.show("Saved product type: ${saved.name}", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            clearForm()
        } catch (ex: ValidationException) {
            Notification.show(ex.message ?: "Validation failed", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
        }
    }

    private fun clearForm() {
        nameField.clear()
        requiresUnderlyingField.value = true
        requiresMaturityField.value = true
        legRowsLayout.removeAll()
        legRows.clear()
    }
}
