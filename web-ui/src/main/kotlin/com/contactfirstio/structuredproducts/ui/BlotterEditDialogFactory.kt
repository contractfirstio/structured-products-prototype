package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.service.ActiveProductSummary
import com.contactfirstio.structuredproducts.service.BlotterService
import com.contactfirstio.structuredproducts.service.ContractHydrationException
import com.contactfirstio.structuredproducts.service.CreateProductTypeCommand
import com.contactfirstio.structuredproducts.service.GlobalTermsSchemaDto
import com.contactfirstio.structuredproducts.service.LegSchemaDto
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
import com.vaadin.flow.theme.lumo.LumoUtility
import org.springframework.stereotype.Component

@Component
class BlotterEditDialogFactory(
    private val blotterService: BlotterService,
) {

    fun openProductTypeEdit(typeId: String, onSaved: () -> Unit) {
        val detail = blotterService.getProductTypeForEdit(typeId)
        val legOptions = blotterService.availableLegProcessorOptions()

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
        val legRows = mutableListOf<Triple<ComboBox<String>, Checkbox, HorizontalLayout>>()

        fun addLegRow(legType: String?, mandatory: Boolean) {
            val legTypeField = ComboBox<String>("Leg Type").apply {
                setItems(legOptions.map { it.schemaLegType })
                setItemLabelGenerator { schemaType ->
                    legOptions.firstOrNull { it.schemaLegType == schemaType }?.displayName ?: schemaType
                }
                value = legType ?: legOptions.firstOrNull()?.schemaLegType
                width = "100%"
            }
            val mandatoryField = Checkbox("Mandatory").apply { value = mandatory }
            val row = HorizontalLayout().apply {
                setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END)
                width = "100%"
            }
            val removeButton = Button(VaadinIcon.TRASH.create()) {
                legRowsLayout.remove(row)
                legRows.removeIf { it.third === row }
            }.apply { addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON) }
            row.add(legTypeField, mandatoryField, removeButton)
            row.expand(legTypeField)
            legRows += Triple(legTypeField, mandatoryField, row)
            legRowsLayout.add(row)
        }

        detail.legSchemas.forEach { leg -> addLegRow(leg.legType, leg.isRequired) }
        if (legRows.isEmpty()) addLegRow(null, false)

        val dialog = Dialog().apply {
            headerTitle = "Edit Product Type"
            width = "720px"
            add(
                FormLayout(nameField, requiresUnderlyingField, requiresMaturityField),
                H3("Allowed Legs"),
                legRowsLayout,
                Button("Add Leg", VaadinIcon.PLUS.create()) { addLegRow(null, false) },
            )
            footer.add(
                Button("Cancel") { close() },
                Button("Save") {
                    runCatching {
                        val legSchemas = legRows.mapNotNull { (legTypeField, mandatoryField, _) ->
                            val legType = legTypeField.value ?: return@mapNotNull null
                            LegSchemaDto(legType, mandatoryField.value, "", "")
                        }
                        blotterService.updateProductType(
                            typeId,
                            CreateProductTypeCommand(
                                name = nameField.value,
                                globalTermsSchema = GlobalTermsSchemaDto(
                                    requiresUnderlying = requiresUnderlyingField.value,
                                    requiresMaturityDate = requiresMaturityField.value,
                                ),
                                legSchemas = legSchemas,
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
            detail.legValues,
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
            is ValidationException, is ContractHydrationException -> ex.message
            else -> ex.message ?: "Update failed"
        }
        Notification.show(message ?: "Update failed", 5000, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_ERROR)
    }
}
