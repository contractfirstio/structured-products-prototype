package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.service.ActiveProductSummary
import com.contactfirstio.structuredproducts.service.BlotterService
import com.contactfirstio.structuredproducts.service.CreateLegSchemaCommand
import com.contactfirstio.structuredproducts.service.CreateProductTypeCommand
import com.contactfirstio.structuredproducts.service.FieldDefinitionDto
import com.contactfirstio.structuredproducts.service.GlobalTermsSchemaDto
import com.contactfirstio.structuredproducts.service.LegSchemaDetail
import com.contactfirstio.structuredproducts.service.ProductTypeDetail
import com.contactfirstio.structuredproducts.service.ValidationException
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextField
import org.springframework.stereotype.Component

@Component
class BlotterEditDialogFactory(
    private val blotterService: BlotterService,
) {

    private data class FieldRow(
        val layout: HorizontalLayout,
        val fieldNameField: TextField,
        val dataTypeField: ComboBox<FieldDataType>,
        val requiredField: Checkbox,
        val enumOptionsField: TextField,
    )

    fun openLegSchemaEdit(legSchemaId: String, onSaved: () -> Unit) {
        val detail = blotterService.getLegSchemaForEdit(legSchemaId)

        val nameField = TextField("Leg Name").apply {
            value = detail.name
            width = "100%"
        }
        val fieldRowsLayout = VerticalLayout().apply { isPadding = false }
        val fieldRows = mutableListOf<FieldRow>()

        fun addFieldRow(
            fieldName: String = "",
            dataType: FieldDataType = FieldDataType.STRING,
            required: Boolean = true,
            enumOptions: String = "",
        ) {
            val fieldNameField = TextField("Field Name").apply {
                value = fieldName
                width = "100%"
            }
            val dataTypeField = ComboBox<FieldDataType>("Data Type").apply {
                setItems(FieldDataType.entries)
                value = dataType
                width = "100%"
            }
            val requiredField = Checkbox("Required").apply { value = required }
            val enumOptionsField = TextField("Enum Options (comma-separated)").apply {
                value = enumOptions
                width = "100%"
                isVisible = dataType == FieldDataType.ENUM
            }

            dataTypeField.addValueChangeListener { event ->
                enumOptionsField.isVisible = event.value == FieldDataType.ENUM
            }

            val rowLayout = HorizontalLayout().apply {
                setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END)
                width = "100%"
            }

            val fieldRow = FieldRow(rowLayout, fieldNameField, dataTypeField, requiredField, enumOptionsField)
            val removeButton = Button(VaadinIcon.TRASH.create()) {
                fieldRowsLayout.remove(rowLayout)
                fieldRows.remove(fieldRow)
            }.apply { addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON) }

            rowLayout.add(fieldNameField, dataTypeField, requiredField, enumOptionsField, removeButton)
            rowLayout.expand(fieldNameField)
            fieldRows += fieldRow
            fieldRowsLayout.add(rowLayout)
        }

        detail.fields.forEach { field ->
            addFieldRow(
                fieldName = field.fieldName,
                dataType = field.dataType,
                required = field.isRequired,
                enumOptions = field.enumOptions.joinToString(", "),
            )
        }
        if (fieldRows.isEmpty()) addFieldRow()

        val dialog = Dialog().apply {
            headerTitle = "Edit Leg Schema"
            width = "900px"
            add(
                FormLayout(nameField),
                H3("Field Definitions"),
                fieldRowsLayout,
                Button("Add Field", VaadinIcon.PLUS.create()) { addFieldRow() },
            )
            footer.add(
                Button("Cancel") { close() },
                Button("Save") {
                    runCatching {
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

                        blotterService.updateLegSchema(
                            legSchemaId,
                            CreateLegSchemaCommand(
                                name = nameField.value,
                                fields = fields,
                            ),
                        )
                    }.onSuccess {
                        close()
                        showSuccess("Leg schema updated")
                        onSaved()
                    }.onFailure { ex -> showError(ex) }
                }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) },
            )
        }
        dialog.open()
    }

    fun openProductTypeEdit(typeId: String, onSaved: () -> Unit) {
        val detail = blotterService.getProductTypeForEdit(typeId)
        val legOptions = blotterService.availableLegSchemas()

        val nameField = TextField("Product Type Name").apply {
            value = detail.name
            width = "100%"
        }
        val requiresUnderlyingField = Checkbox("Requires Underlying").apply {
            value = detail.globalTermsSchema.requiresUnderlying
        }
        val requiresMaturityField = Checkbox("Requires Maturity Date").apply {
            value = detail.globalTermsSchema.requiresMaturityDate
        }
        val legRowsLayout = VerticalLayout().apply { isPadding = false }
        val legRows = mutableListOf<Pair<ComboBox<LegSchemaDetail>, HorizontalLayout>>()

        fun addLegRow(legSchema: LegSchemaDetail?) {
            val legSchemaField = ComboBox<LegSchemaDetail>("Leg Schema").apply {
                setItems(legOptions)
                setItemLabelGenerator { it.name }
                value = legSchema ?: legOptions.firstOrNull()
                width = "100%"
            }
            val row = HorizontalLayout().apply {
                setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END)
                width = "100%"
            }
            val removeButton = Button(VaadinIcon.TRASH.create()) {
                legRowsLayout.remove(row)
                legRows.removeIf { it.second === row }
            }.apply { addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON) }
            row.add(legSchemaField, removeButton)
            row.expand(legSchemaField)
            legRows += legSchemaField to row
            legRowsLayout.add(row)
        }

        detail.legSchemas.forEach { leg -> addLegRow(leg) }
        if (legRows.isEmpty()) addLegRow(null)

        val dialog = Dialog().apply {
            headerTitle = "Edit Product Type"
            width = "720px"
            add(
                FormLayout(nameField, requiresUnderlyingField, requiresMaturityField),
                H3("Allowed Leg Schemas"),
                legRowsLayout,
                Button("Add Leg Schema", VaadinIcon.PLUS.create()) { addLegRow(null) },
            )
            footer.add(
                Button("Cancel") { close() },
                Button("Save") {
                    runCatching {
                        val allowedLegSchemaIds = legRows.mapNotNull { (field, _) ->
                            field.value?.id
                        }.distinct()
                        blotterService.updateProductType(
                            typeId,
                            CreateProductTypeCommand(
                                name = nameField.value,
                                globalTermsSchema = GlobalTermsSchemaDto(
                                    requiresUnderlying = requiresUnderlyingField.value,
                                    requiresMaturityDate = requiresMaturityField.value,
                                ),
                                allowedLegSchemaIds = allowedLegSchemaIds,
                            ),
                        )
                    }.onSuccess {
                        close()
                        showSuccess("Product type updated")
                        onSaved()
                    }.onFailure { ex -> showError(ex) }
                }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) },
            )
        }
        dialog.open()
    }

    fun openProductInstanceEdit(productId: String, onSaved: () -> Unit) {
        val detail = blotterService.getProductInstanceForEdit(productId)
        val formLayout = FormLayout()
        val formBuilder = DynamicProductFormBuilder()
        formBuilder.build(
            formLayout,
            detail.productType,
            detail.underlying,
            detail.maturityMonths,
            detail.legData,
        )

        val dialog = Dialog().apply {
            headerTitle = "Edit Product Instance"
            width = "640px"
            add(formLayout)
            footer.add(
                Button("Cancel") { close() },
                Button("Save") {
                    if (!formBuilder.validate()) {
                        showError(ValidationException("Please complete all required fields"))
                        return@Button
                    }
                    runCatching {
                        blotterService.updateProductInstance(productId, formBuilder.toCommand(detail.typeId))
                    }.onSuccess {
                        close()
                        showSuccess("Product instance updated")
                        onSaved()
                    }.onFailure { ex -> showError(ex) }
                }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) },
            )
        }
        dialog.open()
    }

    fun openOrderEdit(orderId: String, onSaved: () -> Unit) {
        val detail = blotterService.getOrderForEdit(orderId)
        val productField = ComboBox<ActiveProductSummary>("Active Product").apply {
            setItems(detail.activeProducts)
            setItemLabelGenerator { "${it.underlying} (${it.maturityMonths}m)" }
            value = detail.activeProducts.firstOrNull { it.id == detail.productId }
            width = "100%"
        }
        val notionalField = NumberField("Notional Amount").apply {
            value = detail.notionalInvested
            min = 0.0
            step = 1000.0
        }

        val dialog = Dialog().apply {
            headerTitle = "Edit Order"
            width = "520px"
            add(FormLayout(productField, notionalField))
            footer.add(
                Button("Cancel") { close() },
                Button("Save") {
                    val product = productField.value
                    if (product == null || notionalField.isEmpty) {
                        showError(ValidationException("Product and notional are required"))
                        return@Button
                    }
                    runCatching {
                        blotterService.updateOrder(orderId, product.id, notionalField.value)
                    }.onSuccess {
                        close()
                        showSuccess("Order updated")
                        onSaved()
                    }.onFailure { ex -> showError(ex) }
                }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) },
            )
        }
        dialog.open()
    }

    private fun showSuccess(message: String) {
        Notification.show(message, 3000, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS)
    }

    private fun showError(ex: Throwable) {
        val message = when (ex) {
            is ValidationException -> ex.message
            else -> ex.message ?: "Update failed"
        }
        Notification.show(message ?: "Update failed", 5000, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_ERROR)
    }
}
