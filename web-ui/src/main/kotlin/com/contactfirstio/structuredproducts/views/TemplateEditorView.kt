package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.data.document.TemplateStatus
import com.contactfirstio.structuredproducts.service.FieldCatalogService
import com.contactfirstio.structuredproducts.service.ProductTemplateService
import com.contactfirstio.structuredproducts.service.SaveProductTemplateCommand
import com.contactfirstio.structuredproducts.service.ValidationException
import com.contactfirstio.structuredproducts.ui.CaCaaDeclarationQuestionsPanel
import com.contactfirstio.structuredproducts.ui.CommonFieldsPanel
import com.contactfirstio.structuredproducts.ui.StandardFieldsPanel
import com.contactfirstio.structuredproducts.ui.TemplateDefaultFieldFactory
import com.contactfirstio.structuredproducts.ui.UiComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.tabs.TabsVariant
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.OptionalParameter
import com.vaadin.flow.router.Route
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Route(value = "admin/templates/edit/:templateId?", layout = MainLayout::class)
class TemplateEditorView(
    private val productTemplateService: ProductTemplateService,
    private val fieldCatalogService: FieldCatalogService,
    private val templateDefaultFieldFactory: TemplateDefaultFieldFactory,
) : VerticalLayout(),
    HasUrlParameter<String> {

    private var templateId: String? = null

    private val nameField =
        TextField().apply {
            placeholder = "Template name"
            element.setAttribute("aria-label", "Template name")
            isRequired = true
            addClassName("template-metadata-name")
        }
    private val descriptionField =
        TextField().apply {
            placeholder = "Description (optional)"
            element.setAttribute("aria-label", "Description")
            addClassName("template-metadata-description")
        }
    private val statusField =
        ComboBox<TemplateStatus>().apply {
            placeholder = "Status"
            element.setAttribute("aria-label", "Status")
            setItems(TemplateStatus.entries)
            setItemLabelGenerator { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
            value = TemplateStatus.DRAFT
            addClassName("template-metadata-status")
        }

    private val standardDefaults = mutableMapOf<String, String>()
    private val includedCommonKeys = mutableSetOf<String>()
    private val caCaaDeclarationQuestions = mutableListOf<String>()

    private lateinit var standardFieldsPanel: StandardFieldsPanel
    private lateinit var commonFieldsPanel: CommonFieldsPanel
    private lateinit var caCaaPanel: CaCaaDeclarationQuestionsPanel
    private lateinit var standardTabContent: VerticalLayout
    private lateinit var commonTabContent: VerticalLayout
    private lateinit var caCaaTabContent: VerticalLayout
    private lateinit var tabs: Tabs

    init {
        addClassName("template-editor-view")
        setWidthFull()
        isPadding = false
    }

    override fun setParameter(event: BeforeEvent, @OptionalParameter parameter: String?) {
        templateId = parameter?.takeIf { it.isNotBlank() }

        standardDefaults.clear()
        includedCommonKeys.clear()
        caCaaDeclarationQuestions.clear()

        val detail = templateId?.let { productTemplateService.findById(it) }
        if (detail == null && templateId != null) {
            Notification.show("Template not found", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            event.forwardTo(TemplateListView::class.java)
            return
        }

        if (detail != null) {
            standardDefaults.putAll(detail.standardFieldDefaults)
            includedCommonKeys.addAll(detail.includedCommonFieldKeys)
            caCaaDeclarationQuestions.addAll(detail.caCaaDeclarationQuestions)
        } else {
            applySuggestedDefaults()
        }

        buildUi(detail)
    }

    private fun buildUi(detail: com.contactfirstio.structuredproducts.service.ProductTemplateDetail?) {
        removeAll()

        standardFieldsPanel =
            StandardFieldsPanel(
                fieldCatalogService,
                templateDefaultFieldFactory,
                standardDefaults,
                templateId,
            )
        commonFieldsPanel = CommonFieldsPanel(fieldCatalogService, includedCommonKeys)
        caCaaPanel = CaCaaDeclarationQuestionsPanel(caCaaDeclarationQuestions)

        standardTabContent =
            VerticalLayout(standardFieldsPanel).apply {
                isPadding = false
                setWidthFull()
            }
        commonTabContent =
            VerticalLayout(commonFieldsPanel).apply {
                isPadding = false
                setWidthFull()
            }
        caCaaTabContent =
            VerticalLayout(caCaaPanel).apply {
                isPadding = false
                setWidthFull()
            }

        val standardCount = fieldCatalogService.standardFields().size
        val standardTab = Tab("Standard fields ($standardCount)")
        val commonTab = Tab("Product specific fields")
        val caCaaTab = Tab("CA/CAA declaration")
        tabs =
            Tabs(standardTab, commonTab, caCaaTab).apply {
                addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS)
                setWidthFull()
                addSelectedChangeListener { event ->
                    standardTabContent.isVisible = event.selectedTab == standardTab
                    commonTabContent.isVisible = event.selectedTab == commonTab
                    caCaaTabContent.isVisible = event.selectedTab == caCaaTab
                }
            }

        commonTabContent.isVisible = false
        caCaaTabContent.isVisible = false

        val saveButton =
            Button("Save") { save() }.apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            }
        val cancelButton =
            Button("Cancel") {
                ui.ifPresent { it.navigate(TemplateListView::class.java) }
            }
        val deleteButton =
            Button("Delete", com.vaadin.flow.component.icon.VaadinIcon.TRASH.create()) {
                confirmDelete()
            }.apply {
                isVisible = templateId != null
                addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY)
            }

        val metadataBar =
            HorizontalLayout(
                nameField,
                descriptionField,
                statusField,
                HorizontalLayout(saveButton, cancelButton, deleteButton).apply {
                    addClassName("template-metadata-actions")
                    isPadding = false
                    setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
                },
            ).apply {
                addClassName("template-metadata-bar")
                setWidthFull()
                isPadding = false
                setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
                expand(nameField)
                expand(descriptionField)
            }

        val fieldsPanel =
            UiComponents.glassPanel("template-fields-container").apply {
                setWidthFull()
                add(tabs, standardTabContent, commonTabContent, caCaaTabContent)
            }

        val shell = UiComponents.pageShell(wide = true)
        shell.add(
            metadataBar,
            fieldsPanel,
        )
        add(shell)

        if (detail != null) {
            nameField.value = detail.name
            descriptionField.value = detail.description
            statusField.value = detail.status
        } else {
            nameField.clear()
            descriptionField.clear()
            statusField.value = TemplateStatus.DRAFT
        }
    }

    private fun applySuggestedDefaults() {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        fieldCatalogService.standardFields()
            .filter { it.defaultedInTemplateCreation }
            .forEach { field ->
                when (field.key) {
                    "product_status" -> standardDefaults[field.key] = "Draft"
                    "booking_center" -> standardDefaults[field.key] = "HK/SG"
                    "market_segment" -> standardDefaults[field.key] = "Institutional"
                    "itm_settlement_type" -> standardDefaults[field.key] = "Cash"
                    "effective_from" -> standardDefaults[field.key] = today.format(dateFormatter)
                    "effective_to" -> standardDefaults[field.key] = today.plusYears(10).format(dateFormatter)
                }
            }
    }

    private fun save() {
        if (nameField.isEmpty) {
            nameField.isInvalid = true
            Notification.show("Template name is required", 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
            return
        }
        nameField.isInvalid = false

        val command =
            SaveProductTemplateCommand(
                name = nameField.value,
                description = descriptionField.value.orEmpty(),
                status = statusField.value ?: TemplateStatus.DRAFT,
                standardFieldDefaults = standardDefaults.toMap(),
                includedCommonFieldKeys = includedCommonKeys.toSet(),
                caCaaDeclarationQuestions = caCaaDeclarationQuestions.toList(),
            )

        try {
            val saved =
                if (templateId == null) {
                    productTemplateService.create(command)
                } else {
                    productTemplateService.update(templateId!!, command)
                }
            Notification.show("Saved template: ${saved.name}", 3000, Notification.Position.BOTTOM_START)
            ui.ifPresent { it.navigate(TemplateEditorView::class.java, saved.id) }
        } catch (ex: ValidationException) {
            Notification.show(ex.message ?: "Validation failed", 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
        }
    }

    private fun confirmDelete() {
        val id = templateId ?: return
        ConfirmDialog().apply {
            setHeader("Delete template?")
            setText("Delete \"${nameField.value}\"? This cannot be undone.")
            setCancelable(true)
            setConfirmText("Delete")
            setConfirmButtonTheme("error primary")
            addConfirmListener {
                try {
                    productTemplateService.delete(id)
                    Notification.show("Template deleted", 3000, Notification.Position.BOTTOM_START)
                    ui.ifPresent { it.navigate(TemplateListView::class.java) }
                } catch (ex: ValidationException) {
                    Notification.show(ex.message ?: "Delete failed", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR)
                }
            }
        }.open()
    }
}
