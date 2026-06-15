package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.service.ActiveProductSummary
import com.contactfirstio.structuredproducts.service.OrderService
import com.contactfirstio.structuredproducts.service.ProductQueryService
import com.contactfirstio.structuredproducts.service.ValidationException
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.router.Route

@Route(value = "order-entry", layout = MainLayout::class)
class OrderEntryView(
    private val productQueryService: ProductQueryService,
    private val orderService: OrderService,
) : VerticalLayout() {

    private val productField = ComboBox<ActiveProductSummary>("Active Product").apply {
        setItems(productQueryService.findActiveProducts())
        setItemLabelGenerator { "${it.underlying} (${it.maturityMonths}m)" }
        isRequired = true
        setWidthFull()
    }
    private val notionalField = NumberField("Notional Amount").apply {
        isRequired = true
        min = 0.0
        step = 1000.0
        prefixComponent = Span("$")
    }
    private val submitButton = Button("Submit Order") { submit() }

    init {
        val shell = com.contactfirstio.structuredproducts.ui.UiComponents.pageShell()
        val panel = com.contactfirstio.structuredproducts.ui.UiComponents.glassPanel("form-panel")
        panel.add(
            FormLayout().apply {
                setResponsiveSteps(FormLayout.ResponsiveStep("0", 1))
                add(productField, notionalField)
            },
            submitButton.apply { addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY) },
        )
        shell.add(
            com.contactfirstio.structuredproducts.ui.UiComponents.pageHeader(
                eyebrow = "Level 3 · Execution",
                title = "Order Entry",
                subtitle = "Select an ACTIVE product and enter notional to submit a validated trade.",
            ),
            panel,
        )
        add(shell)
        setWidthFull()
        isPadding = false
    }

    private fun submit() {
        if (productField.isEmpty || notionalField.isEmpty) {
            productField.isInvalid = productField.isEmpty
            notionalField.isInvalid = notionalField.isEmpty
            Notification.show("Please select a product and enter a notional amount", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            return
        }

        val product = productField.value
        val notional = notionalField.value

        try {
            val result = orderService.submitOrder(product.id, notional)
            Notification.show(
                "Order submitted for ${result.productUnderlying}",
                4000,
                Notification.Position.MIDDLE,
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            notionalField.clear()
            refreshProducts()
        } catch (ex: ValidationException) {
            Notification.show(ex.message ?: "Unable to submit order", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
        }
    }

    private fun refreshProducts() {
        val selectedId = productField.value?.id
        val activeProducts = productQueryService.findActiveProducts()
        productField.setItems(activeProducts)
        productField.value = activeProducts.firstOrNull { it.id == selectedId }
    }
}
