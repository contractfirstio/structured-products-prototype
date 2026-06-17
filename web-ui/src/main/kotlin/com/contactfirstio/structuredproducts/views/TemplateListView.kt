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
import com.vaadin.flow.router.Route
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Route(value = "admin/templates", layout = MainLayout::class)
class TemplateListView(
    private val productTemplateService: ProductTemplateService,
) : VerticalLayout() {

    private val grid =
        Grid(ProductTemplateSummary::class.java, false).apply {
            addClassName("template-list-grid")
            setWidthFull()

            addColumn(ProductTemplateSummary::name)
                .setHeader("Name")
                .setFlexGrow(2)

            addColumn(ProductTemplateSummary::description)
                .setHeader("Description")
                .setFlexGrow(2)

            addColumn { "${it.standardFieldCount} standard" }
                .setHeader("Standard fields")
                .setFlexGrow(0)

            addColumn { "${it.commonFieldsSelected} of ${it.commonFieldsTotal}" }
                .setHeader("Product specific fields")
                .setFlexGrow(0)

            addColumn { "${it.caCaaDeclarationQuestionCount} questions" }
                .setHeader("CA/CAA")
                .setFlexGrow(0)

            addColumn { it.status.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                .setHeader("Status")
                .setFlexGrow(0)

            addColumn {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(it.updatedAt)
            }
                .setHeader("Updated")
                .setFlexGrow(0)

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
                    isPadding = false
                }
            }
                .setHeader("Actions")
                .setFlexGrow(0)
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
        refreshGrid()
    }

    private fun refreshGrid() {
        grid.setItems(productTemplateService.findAll())
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
}
