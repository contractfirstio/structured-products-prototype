package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.service.FieldCatalogService
import com.contactfirstio.structuredproducts.service.ProductTemplateService
import com.contactfirstio.structuredproducts.service.ProductTemplateSummary
import com.contactfirstio.structuredproducts.service.ValidationException
import com.contactfirstio.structuredproducts.ui.TemplateDetailPanel
import com.contactfirstio.structuredproducts.ui.UiComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.contactfirstio.structuredproducts.ui.ConfirmDialogs
import com.contactfirstio.structuredproducts.ui.DisplayFormatters
import com.vaadin.flow.component.grid.AbstractGridSingleSelectionModel
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.splitlayout.SplitLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteParam
import com.vaadin.flow.router.RouteParameters

@Route(value = "admin/templates", layout = MainLayout::class)
class TemplateListView(
    private val productTemplateService: ProductTemplateService,
    private val fieldCatalogService: FieldCatalogService,
) : VerticalLayout(),
    BeforeEnterObserver {

    private var highlightedTemplateId: String? = null
    private var selectedTemplateId: String? = null
    private var detailPanelVisible = false
    private var savedSplitterPosition = 72.0

    private lateinit var detailPanel: TemplateDetailPanel
    private lateinit var splitLayout: SplitLayout
    private lateinit var toggleDetailButton: Button

    private val grid =
        Grid(ProductTemplateSummary::class.java, false).apply {
            addClassName("template-list-grid")
            setWidthFull()
            selectionMode = Grid.SelectionMode.SINGLE
            (selectionModel as AbstractGridSingleSelectionModel<ProductTemplateSummary>)
                .setDeselectAllowed(false)

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

            addColumn { DisplayFormatters.enumLabel(it.status) }
                .setHeader("Status")
                .setFlexGrow(0)
                .setWidth("5.5rem")
                .setAutoWidth(false)

            addColumn { DisplayFormatters.formatInstant(it.updatedAt) }
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
        detailPanel =
            TemplateDetailPanel(fieldCatalogService) { templateId ->
                navigateToEditor(templateId)
            }

        grid.addSelectionListener { event ->
            val selected = event.firstSelectedItem.orElse(null)
            if (selected != null) {
                selectedTemplateId = selected.id
                showDetailFor(selected.id)
            } else {
                selectedTemplateId = null
                detailPanel.showEmpty()
            }
        }

        toggleDetailButton =
            Button("Show template details", VaadinIcon.ANGLE_LEFT.create()) {
                setDetailPanelVisible(!detailPanelVisible)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL)
                addClassName("template-detail-toggle")
                element.setAttribute("aria-expanded", "false")
                element.setAttribute("aria-controls", "template-detail-panel")
            }

        val listToolbar =
            HorizontalLayout(toggleDetailButton).apply {
                addClassName("template-list-toolbar")
                setWidthFull()
                isPadding = false
                setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
                setJustifyContentMode(FlexComponent.JustifyContentMode.END)
            }

        val listPanel =
            UiComponents.glassPanel("template-list-panel", "template-list-grid-panel").apply {
                setWidthFull()
                add(listToolbar, grid)
            }

        val detailPanelWrapper =
            UiComponents.glassPanel("template-detail-panel").apply {
                add(detailPanel)
            }

        splitLayout =
            SplitLayout().apply {
                addClassName("template-list-split")
                setWidthFull()
                setSplitterPosition(savedSplitterPosition)
                setPrimaryStyle("min-width", "18rem")
                setSecondaryStyle("min-width", "16rem")
                addToPrimary(listPanel)
                addToSecondary(detailPanelWrapper)
                addSplitterDragEndListener {
                    if (detailPanelVisible) {
                        savedSplitterPosition = splitterPosition
                    }
                }
                element.executeJs(
                    """
                    const el = this;
                    const mq = window.matchMedia('(max-width: 1100px)');
                    const update = () => {
                        el.orientation = mq.matches ? 'vertical' : 'horizontal';
                    };
                    mq.addEventListener('change', update);
                    update();
                    """.trimIndent(),
                )
            }
        detailPanelWrapper.element.setAttribute("id", "template-detail-panel")
        setDetailPanelVisible(false)

        val shell = UiComponents.pageShell(wide = true)
        shell.add(
            UiComponents.pageHeader(
                title = "Product Templates",
                UiComponents.primaryButton("Create template") { navigateToEditor(null) },
            ),
            splitLayout,
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

        val selectId = highlightId ?: selectedTemplateId
        if (selectId != null) {
            val summary = items.find { it.id == selectId }
            if (summary != null) {
                grid.select(summary)
                grid.scrollToItem(summary)
                showDetailFor(selectId)
            } else {
                grid.deselectAll()
                selectedTemplateId = null
                detailPanel.showEmpty()
            }
        }

        if (highlightId != null) {
            ui.ifPresent { ui ->
                ui.page.history.replaceState(null, Location("admin/templates"), false)
            }
            setDetailPanelVisible(true)
        }
    }

    private fun setDetailPanelVisible(visible: Boolean) {
        detailPanelVisible = visible
        if (visible) {
            splitLayout.setSecondaryStyle("min-width", "16rem")
            splitLayout.setSplitterPosition(savedSplitterPosition)
            splitLayout.removeClassName("template-detail-hidden")
        } else {
            savedSplitterPosition = splitLayout.splitterPosition
            splitLayout.setSecondaryStyle("min-width", "0")
            splitLayout.setSplitterPosition(100.0)
            splitLayout.addClassName("template-detail-hidden")
        }
        if (visible) {
            toggleDetailButton.text = "Hide details"
            toggleDetailButton.icon = VaadinIcon.ANGLE_RIGHT.create()
        } else {
            toggleDetailButton.text = "Show template details"
            toggleDetailButton.icon = VaadinIcon.ANGLE_LEFT.create()
        }
        toggleDetailButton.element.setAttribute("aria-expanded", visible.toString())
    }

    private fun showDetailFor(templateId: String) {
        val detail = productTemplateService.findById(templateId)
        if (detail != null) {
            detailPanel.showDetail(detail)
        } else {
            detailPanel.showEmpty()
        }
    }

    private fun navigateToEditor(templateId: String?) {
        ui.ifPresent { ui ->
            if (templateId == null) {
                ui.navigate(TemplateEditorView::class.java)
            } else {
                ui.navigate(
                    TemplateEditorView::class.java,
                    RouteParameters(RouteParam("templateId", templateId)),
                )
            }
        }
    }

    private fun confirmDelete(summary: ProductTemplateSummary) {
        ConfirmDialogs.confirmDelete(summary.name) {
            try {
                productTemplateService.delete(summary.id)
                Notification.show("Deleted template: ${summary.name}", 3000, Notification.Position.BOTTOM_START)
                refreshGrid()
            } catch (ex: ValidationException) {
                Notification.show(ex.message ?: "Delete failed", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR)
            }
        }
    }

    companion object {
        const val HIGHLIGHT_QUERY_PARAM = "highlight"
    }
}
