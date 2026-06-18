package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.service.ProductTemplateService
import com.contactfirstio.structuredproducts.service.ProductTemplateSummary
import com.contactfirstio.structuredproducts.service.ValidationException
import com.contactfirstio.structuredproducts.ui.UiComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.Route
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Route(value = "admin/templates", layout = MainLayout::class)
class TemplateListView(
    private val productTemplateService: ProductTemplateService,
) : VerticalLayout(),
    BeforeEnterObserver {

    private var highlightedTemplateId: String? = null

    private val grid =
        Grid(ProductTemplateSummary::class.java, false).apply {
            addClassName("template-list-grid")
            setWidthFull()

            addColumn(ProductTemplateSummary::name)
                .setHeader("Name")
                .setFlexGrow(1)
                .setTooltipGenerator(ProductTemplateSummary::name)

            addColumn(ProductTemplateSummary::description)
                .setHeader("Description")
                .setFlexGrow(2)
                .setTooltipGenerator(ProductTemplateSummary::description)

            addColumn { "${it.standardFieldCount}" }
                .setHeader("Std")
                .setFlexGrow(0)
                .setWidth("3rem")
                .setAutoWidth(false)

            addColumn { "${it.commonFieldsSelected}/${it.commonFieldsTotal}" }
                .setHeader("Common")
                .setFlexGrow(0)
                .setWidth("5rem")
                .setAutoWidth(false)

            addColumn { "${it.caCaaDeclarationQuestionCount}" }
                .setHeader("CA/CAA")
                .setFlexGrow(0)
                .setWidth("4.5rem")
                .setAutoWidth(false)

            addColumn { "${it.customFieldCount}" }
                .setHeader("Custom")
                .setFlexGrow(0)
                .setWidth("4.5rem")
                .setAutoWidth(false)

            addColumn { it.status.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                .setHeader("Status")
                .setFlexGrow(0)
                .setWidth("5.5rem")
                .setAutoWidth(false)

            addColumn {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(it.updatedAt)
            }
                .setHeader("Updated")
                .setFlexGrow(0)
                .setWidth("8.5rem")
                .setAutoWidth(false)

            addComponentColumn { summary ->
                HorizontalLayout(
                    Button("Edit", VaadinIcon.EDIT.create()) {
                        navigateToEditor(summary.id)
                    }.apply {
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    },
                    Button(VaadinIcon.TRASH.create()) {
                        confirmDelete(summary)
                    }.apply {
                        addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY)
                        element.setAttribute("aria-label", "Delete ${summary.name}")
                    },
                ).apply {
                    addClassName("template-list-actions")
                    isPadding = false
                    isSpacing = true
                }
            }
                .setHeader("Actions")
                .setFlexGrow(0)
                .setWidth("7.5rem")
                .setAutoWidth(false)
        }

    init {
        val shell = UiComponents.pageShell(wide = true)
        shell.add(
            UiComponents.pageHeader(
                title = "Product Templates",
                UiComponents.primaryButton("Create template") { navigateToEditor(null) },
            ),
            UiComponents.glassPanel("template-list-panel").apply {
                setWidthFull()
                add(grid)
            },
        )
        add(shell)
        setWidthFull()
        isPadding = false
        style.set("min-width", "0")
        style.set("overflow", "hidden")
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        val highlightId = event.location.queryParameters.parameters[HIGHLIGHT_QUERY_PARAM]?.firstOrNull()
        refreshGrid(highlightId)
    }

    private fun refreshGrid(highlightId: String? = null) {
        highlightedTemplateId = highlightId
        val items = productTemplateService.findAll()
        grid.setPartNameGenerator { summary: ProductTemplateSummary ->
            if (summary.id == highlightedTemplateId) "row-new" else null
        }
        grid.setItems(items)

        if (highlightId != null) {
            items.find { it.id == highlightId }?.let { summary ->
                grid.scrollToItem(summary)
            }
            ui.ifPresent { ui ->
                ui.page.history.replaceState(null, Location("admin/templates"), false)
            }
        }
    }

    private fun navigateToEditor(templateId: String?) {
        ui.ifPresent { ui ->
            if (templateId == null) {
                ui.navigate(TemplateEditorView::class.java)
            } else {
                ui.navigate(TemplateEditorView::class.java, templateId)
            }
        }
    }

    private fun confirmDelete(summary: ProductTemplateSummary) {
        ConfirmDialog().apply {
            setHeader("Delete template?")
            setText("Delete \"${summary.name}\"? This cannot be undone.")
            setCancelable(true)
            setConfirmText("Delete")
            setConfirmButtonTheme("error primary")
            addConfirmListener {
                try {
                    productTemplateService.delete(summary.id)
                    Notification.show("Deleted template: ${summary.name}", 3000, Notification.Position.BOTTOM_START)
                    refreshGrid()
                } catch (ex: ValidationException) {
                    Notification.show(ex.message ?: "Delete failed", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR)
                }
            }
        }.open()
    }

    companion object {
        const val HIGHLIGHT_QUERY_PARAM = "highlight"
    }
}
