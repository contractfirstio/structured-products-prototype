package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.service.BlotterService
import com.contactfirstio.structuredproducts.service.OrderBlotterRow
import com.contactfirstio.structuredproducts.service.ProductInstanceBlotterRow
import com.contactfirstio.structuredproducts.service.ProductTypeBlotterRow
import com.contactfirstio.structuredproducts.service.ValidationException
import com.contactfirstio.structuredproducts.ui.BlotterEditDialogFactory
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility
import java.text.NumberFormat

@Route(value = "blotter", layout = MainLayout::class)
class BlotterView(
    private val blotterService: BlotterService,
    private val editDialogFactory: BlotterEditDialogFactory,
) : VerticalLayout() {

    private val currencyFormat = NumberFormat.getCurrencyInstance()
    private val productTypeGrid = createProductTypeGrid()
    private val productInstanceGrid = createProductInstanceGrid()
    private val orderGrid = createOrderGrid()

    init {
        setSizeFull()
        isPadding = false

        val shell = com.contactfirstio.structuredproducts.ui.UiComponents.pageShell(wide = true)
        shell.add(
            com.contactfirstio.structuredproducts.ui.UiComponents.pageHeader(
                eyebrow = "Operations",
                title = "Data Blotter",
                subtitle = "Review, edit, and delete product types, product instances, and orders. Types with instances and instances with orders are protected.",
                Button("Refresh", VaadinIcon.REFRESH.create()) { refresh() },
            ),
            createSection("Product Types (Level 1)", productTypeGrid),
            createSection("Product Instances (Level 2)", productInstanceGrid),
            createSection("Orders (Level 3)", orderGrid),
        )
        add(shell)

        refresh()
    }

    private fun createSection(title: String, grid: Grid<*>): Div =
        Div().apply {
            addClassName("blotter-section")
            add(
                H3(title),
                grid,
            )
        }

    private fun createProductTypeGrid(): Grid<ProductTypeBlotterRow> =
        Grid(ProductTypeBlotterRow::class.java, false).apply {
            addColumn(ProductTypeBlotterRow::id).setHeader("ID").setFlexGrow(1)
            addColumn(ProductTypeBlotterRow::name).setHeader("Name").setFlexGrow(2)
            addColumn(ProductTypeBlotterRow::requiresUnderlying).setHeader("Underlying").setFlexGrow(0).setWidth("120px")
            addColumn(ProductTypeBlotterRow::requiresMaturity).setHeader("Maturity").setFlexGrow(0).setWidth("120px")
            addColumn(ProductTypeBlotterRow::legsSummary).setHeader("Legs").setFlexGrow(3)
            addComponentColumn { row -> actionButtons("product-type", row.id, row.canEdit, row.canDelete, { editDialogFactory.openProductTypeEdit(row.id) { refresh() } }, { blotterService.deleteProductType(row.id) }) }
                .setHeader("")
                .setFlexGrow(0)
                .setWidth("120px")
            addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES)
            setAllRowsVisible(true)
            width = "100%"
        }

    private fun createProductInstanceGrid(): Grid<ProductInstanceBlotterRow> =
        Grid(ProductInstanceBlotterRow::class.java, false).apply {
            addColumn(ProductInstanceBlotterRow::id).setHeader("ID").setFlexGrow(1)
            addColumn(ProductInstanceBlotterRow::typeName).setHeader("Type").setFlexGrow(1)
            addColumn(ProductInstanceBlotterRow::underlying).setHeader("Underlying").setFlexGrow(0).setWidth("120px")
            addColumn(ProductInstanceBlotterRow::maturityMonths).setHeader("Maturity").setFlexGrow(0).setWidth("100px")
            addComponentColumn { row -> statusBadge(row.status) }.setHeader("Status").setFlexGrow(0).setWidth("100px")
            addColumn(ProductInstanceBlotterRow::legsSummary).setHeader("Legs").setFlexGrow(3)
            addComponentColumn { row ->
                actionButtons(
                    "product-instance",
                    row.id,
                    row.canEdit,
                    row.canDelete,
                    { editDialogFactory.openProductInstanceEdit(row.id) { refresh() } },
                    { blotterService.deleteProductInstance(row.id) },
                )
            }
                .setHeader("")
                .setFlexGrow(0)
                .setWidth("120px")
            addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES)
            setAllRowsVisible(true)
            width = "100%"
        }

    private fun createOrderGrid(): Grid<OrderBlotterRow> =
        Grid(OrderBlotterRow::class.java, false).apply {
            addColumn(OrderBlotterRow::id).setHeader("ID").setFlexGrow(1)
            addColumn(OrderBlotterRow::productId).setHeader("Product ID").setFlexGrow(1)
            addColumn(OrderBlotterRow::productUnderlying).setHeader("Underlying").setFlexGrow(0).setWidth("120px")
            addColumn { row -> currencyFormat.format(row.notionalInvested) }.setHeader("Notional").setFlexGrow(0).setWidth("140px")
            addComponentColumn { row -> statusBadge(row.status) }.setHeader("Status").setFlexGrow(0).setWidth("120px")
            addComponentColumn { row ->
                actionButtons(
                    "order",
                    row.id,
                    row.canEdit,
                    row.canDelete,
                    { editDialogFactory.openOrderEdit(row.id) { refresh() } },
                    { blotterService.deleteOrder(row.id) },
                )
            }
                .setHeader("")
                .setFlexGrow(0)
                .setWidth("120px")
            addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES)
            setAllRowsVisible(true)
            width = "100%"
        }

    private fun actionButtons(
        entity: String,
        recordId: String,
        canEdit: Boolean,
        canDelete: Boolean,
        editAction: () -> Unit,
        deleteAction: () -> Unit,
    ): HorizontalLayout =
        HorizontalLayout(
            editButton(entity, recordId, canEdit, editAction),
            deleteButton(entity, recordId, canDelete, deleteAction),
        ).apply {
            addClassNames(LumoUtility.Gap.XSMALL)
            isPadding = false
        }

    private fun editButton(entity: String, recordId: String, enabled: Boolean, editAction: () -> Unit): Button =
        Button().apply {
            element.setAttribute("data-testid", "blotter-edit-$entity-$recordId")
            icon = VaadinIcon.EDIT.create()
            addClickListener { editAction() }
            isEnabled = enabled
            addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON)
            element.setAttribute("aria-label", "Edit")
            if (!enabled) {
                element.setAttribute("title", "Cannot edit while dependent records exist")
            }
        }

    private fun deleteButton(entity: String, recordId: String, enabled: Boolean, deleteAction: () -> Unit): Button =
        Button(VaadinIcon.TRASH.create()) {
            ConfirmDialog().apply {
                setHeader("Confirm delete")
                setText("Are you sure you want to delete this record? This action cannot be undone.")
                setCancelable(true)
                setConfirmText("Delete")
                setConfirmButtonTheme("error primary")
                addConfirmListener { runMutation(deleteAction, "Deleted successfully", "Delete failed") }
                open()
            }
        }.apply {
            element.setAttribute("data-testid", "blotter-delete-$entity-$recordId")
            isEnabled = enabled
            addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON)
            element.setAttribute("aria-label", "Delete")
            if (!enabled) {
                element.setAttribute("title", "Cannot delete while dependent records exist")
            }
        }

    private fun runMutation(action: () -> Unit, successMessage: String, failureFallback: String) {
        try {
            action()
            Notification.show(successMessage, 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            refresh()
        } catch (ex: ValidationException) {
            Notification.show(ex.message ?: failureFallback, 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
        }
    }

    private fun statusBadge(status: String): Span =
        Span(status).apply {
            addClassName("blotter-status-badge")
            element.setAttribute("data-status", status)
        }

    private fun refresh() {
        val snapshot = blotterService.loadSnapshot()
        productTypeGrid.setItems(snapshot.productTypes)
        productInstanceGrid.setItems(snapshot.productInstances)
        orderGrid.setItems(snapshot.orders)
    }
}
