package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.service.ContractHydrationException
import com.contactfirstio.structuredproducts.service.ProductInstanceService
import com.contactfirstio.structuredproducts.service.ProductTypeDetail
import com.contactfirstio.structuredproducts.service.ProductTypeService
import com.contactfirstio.structuredproducts.service.ValidationException
import com.contactfirstio.structuredproducts.ui.DynamicProductFormBuilder
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

@Route(value = "create-product", layout = MainLayout::class)
class CreateProductView(
    private val productTypeService: ProductTypeService,
    private val productInstanceService: ProductInstanceService,
) : VerticalLayout() {

    private val productTypes = productTypeService.findAll()
    private val productTypeField = ComboBox<ProductTypeDetail>("Product Type").apply {
        setItems(productTypes)
        setItemLabelGenerator { it.name }
        isRequired = true
        width = "100%"
    }
    private val dynamicFormLayout = FormLayout().apply {
        setResponsiveSteps(FormLayout.ResponsiveStep("0", 1))
    }
    private val dynamicFormBuilder = DynamicProductFormBuilder()
    private val submitButton = Button("Submit") { submit() }.apply {
        isEnabled = false
        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
    }

    init {
        val shell = com.contactfirstio.structuredproducts.ui.UiComponents.pageShell()
        val panel = com.contactfirstio.structuredproducts.ui.UiComponents.glassPanel("form-panel")

        productTypeField.addValueChangeListener { event ->
            val selectedType = event.value
            if (selectedType == null) {
                dynamicFormLayout.removeAll()
                submitButton.isEnabled = false
            } else {
                dynamicFormBuilder.build(dynamicFormLayout, selectedType)
                submitButton.isEnabled = true
            }
        }

        panel.add(productTypeField, dynamicFormLayout, submitButton)
        shell.add(
            com.contactfirstio.structuredproducts.ui.UiComponents.pageHeader(
                eyebrow = "Level 2 · Product Instance",
                title = "Create Product",
                subtitle = "Select a product type to dynamically render instance fields from its Level 1 schema.",
            ),
            panel,
        )
        add(shell)
        setWidthFull()
        isPadding = false
    }

    private fun submit() {
        val selectedType = productTypeField.value
        if (selectedType == null) {
            productTypeField.isInvalid = true
            Notification.show("Select a product type", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            return
        }
        productTypeField.isInvalid = false

        if (!dynamicFormBuilder.validate()) {
            Notification.show("Please complete all required fields", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            return
        }

        try {
            val saved = productInstanceService.saveProduct(
                dynamicFormBuilder.toCommand(selectedType.id),
            )
            Notification.show(
                "Product saved as ${saved.status}",
                4000,
                Notification.Position.MIDDLE,
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            dynamicFormBuilder.clear()
        } catch (ex: ValidationException) {
            Notification.show(
                "Validation failed: ${ex.message}",
                6000,
                Notification.Position.MIDDLE,
            ).addThemeVariants(NotificationVariant.LUMO_ERROR)
        } catch (ex: ContractHydrationException) {
            Notification.show(
                "Validation failed: ${ex.message}",
                6000,
                Notification.Position.MIDDLE,
            ).addThemeVariants(NotificationVariant.LUMO_ERROR)
        }
    }
}
