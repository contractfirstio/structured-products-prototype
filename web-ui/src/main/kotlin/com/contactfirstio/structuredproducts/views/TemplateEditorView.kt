package com.contactfirstio.structuredproducts.views

import com.contactfirstio.structuredproducts.data.document.TemplateCustomFieldDefinition
import com.contactfirstio.structuredproducts.data.document.TemplateStatus
import com.contactfirstio.structuredproducts.service.FieldCatalogService
import com.contactfirstio.structuredproducts.service.ProductTemplateDetail
import com.contactfirstio.structuredproducts.service.ProductTemplateService
import com.contactfirstio.structuredproducts.service.SaveProductTemplateCommand
import com.contactfirstio.structuredproducts.service.ValidationException
import com.contactfirstio.structuredproducts.ui.CaCaaDeclarationQuestionsPanel
import com.contactfirstio.structuredproducts.ui.CommonFieldsPanel
import com.contactfirstio.structuredproducts.ui.ConfirmDialogs
import com.contactfirstio.structuredproducts.ui.CustomFieldsPanel
import com.contactfirstio.structuredproducts.ui.DisplayFormatters
import com.contactfirstio.structuredproducts.ui.StandardFieldsPanel
import com.contactfirstio.structuredproducts.ui.TemplateDefaultFieldFactory
import com.contactfirstio.structuredproducts.ui.UiComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.tabs.TabsVariant
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.QueryParameters
import com.vaadin.flow.router.Route

@Route(value = "admin/templates/edit/:templateId?", layout = MainLayout::class)
class TemplateEditorView(
    private val productTemplateService: ProductTemplateService,
    private val fieldCatalogService: FieldCatalogService,
    private val templateDefaultFieldFactory: TemplateDefaultFieldFactory,
) : VerticalLayout(),
    BeforeEnterObserver {

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
            placeholder = "Description"
            element.setAttribute("aria-label", "Description")
            isRequired = true
            addClassName("template-metadata-description")
        }
    private val statusField =
        ComboBox<TemplateStatus>().apply {
            placeholder = "Status"
            element.setAttribute("aria-label", "Status")
            setItems(TemplateStatus.entries)
            setItemLabelGenerator { DisplayFormatters.enumLabel(it) }
            value = TemplateStatus.DRAFT
            addClassName("template-metadata-status")
        }

    private val standardDefaults = mutableMapOf<String, String>()
    private val includedCommonKeys = mutableSetOf<String>()
    private val mandatoryCommonKeys = mutableSetOf<String>()
    private val caCaaDeclarationQuestions = mutableListOf<String>()
    private val customFields = mutableListOf<TemplateCustomFieldDefinition>()

    private lateinit var standardFieldsPanel: StandardFieldsPanel
    private lateinit var commonFieldsPanel: CommonFieldsPanel
    private lateinit var customFieldsPanel: CustomFieldsPanel
    private lateinit var caCaaPanel: CaCaaDeclarationQuestionsPanel
    private lateinit var standardTabContent: VerticalLayout
    private lateinit var commonTabContent: VerticalLayout
    private lateinit var customTabContent: VerticalLayout
    private lateinit var caCaaTabContent: VerticalLayout
    private lateinit var tabs: Tabs
    private lateinit var standardTab: Tab
    private lateinit var deleteButton: Button

    init {
        addClassName("template-editor-view")
        setWidthFull()
        isPadding = false
        buildUi()
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        templateId =
            event.routeParameters
                .get("templateId")
                .orElse(null)
                ?.takeIf { it.isNotBlank() }

        standardDefaults.clear()
        includedCommonKeys.clear()
        mandatoryCommonKeys.clear()
        caCaaDeclarationQuestions.clear()
        customFields.clear()

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
            mandatoryCommonKeys.addAll(detail.mandatoryCommonFieldKeys)
            caCaaDeclarationQuestions.addAll(detail.caCaaDeclarationQuestions)
            customFields.addAll(detail.customFields)
        } else {
            standardDefaults.putAll(fieldCatalogService.suggestedDefaultsForNewTemplate())
        }

        populateForm(detail)
    }

    private fun buildUi() {
        standardFieldsPanel =
            StandardFieldsPanel(
                fieldCatalogService,
                templateDefaultFieldFactory,
                standardDefaults,
                templateId,
            )
        commonFieldsPanel = CommonFieldsPanel(fieldCatalogService, includedCommonKeys, mandatoryCommonKeys)
        customFieldsPanel = CustomFieldsPanel(customFields)
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
        customTabContent =
            VerticalLayout(customFieldsPanel).apply {
                isPadding = false
                setWidthFull()
            }
        caCaaTabContent =
            VerticalLayout(caCaaPanel.component).apply {
                isPadding = false
                setWidthFull()
            }

        val standardCount = fieldCatalogService.standardFields().size
        standardTab = Tab("Standard fields ($standardCount)")
        val commonTab = Tab("Common fields")
        val customTab = Tab("Custom fields")
        val caCaaTab = Tab("CA/CAA declaration")
        tabs =
            Tabs(standardTab, commonTab, customTab, caCaaTab).apply {
                addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS)
                setWidthFull()
                addSelectedChangeListener { event ->
                    standardTabContent.isVisible = event.selectedTab == standardTab
                    commonTabContent.isVisible = event.selectedTab == commonTab
                    customTabContent.isVisible = event.selectedTab == customTab
                    caCaaTabContent.isVisible = event.selectedTab == caCaaTab
                }
            }

        commonTabContent.isVisible = false
        customTabContent.isVisible = false
        caCaaTabContent.isVisible = false

        val saveButton =
            Button("Save") { save() }.apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            }
        val cancelButton =
            Button("Cancel") {
                ui.ifPresent { it.navigate(TemplateListView::class.java) }
            }
        deleteButton =
            Button("Delete", com.vaadin.flow.component.icon.VaadinIcon.TRASH.create()) {
                confirmDelete()
            }.apply {
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
                add(tabs, standardTabContent, commonTabContent, customTabContent, caCaaTabContent)
            }

        val shell = UiComponents.pageShell(wide = true)
        shell.add(
            metadataBar,
            fieldsPanel,
        )
        add(shell)
    }

    private fun populateForm(detail: ProductTemplateDetail?) {
        nameField.isInvalid = false
        descriptionField.isInvalid = false
        standardFieldsPanel.setShowValidationErrors(false)

        if (detail != null) {
            nameField.value = detail.name
            descriptionField.value = detail.description
            statusField.value = detail.status
        } else {
            nameField.clear()
            descriptionField.clear()
            statusField.value = TemplateStatus.DRAFT
        }

        deleteButton.isVisible = templateId != null
        tabs.selectedTab = standardTab
        standardTabContent.isVisible = true
        commonTabContent.isVisible = false
        customTabContent.isVisible = false
        caCaaTabContent.isVisible = false

        standardFieldsPanel.setTemplateId(templateId)
        standardFieldsPanel.refresh()
        commonFieldsPanel.syncFromIncludedKeys()
        customFieldsPanel.refresh()
        caCaaPanel.refresh()
    }

    private fun save() {
        standardFieldsPanel.setShowValidationErrors(false)
        nameField.isInvalid = false
        descriptionField.isInvalid = false

        val command =
            SaveProductTemplateCommand(
                name = nameField.value,
                description = descriptionField.value.orEmpty(),
                status = statusField.value ?: TemplateStatus.DRAFT,
                standardFieldDefaults = standardDefaults.toMap(),
                includedCommonFieldKeys = includedCommonKeys.toSet(),
                mandatoryCommonFieldKeys = mandatoryCommonKeys.toSet(),
                caCaaDeclarationQuestions = caCaaDeclarationQuestions.toList(),
                customFields = customFields.toList(),
            )

        val validation = fieldCatalogService.validateTemplateForm(command)
        if (!validation.isValid) {
            if (validation.missingFieldLabels.contains("Template name")) {
                nameField.isInvalid = true
                nameField.errorMessage = "Required"
            }
            if (validation.missingFieldLabels.contains("Description")) {
                descriptionField.isInvalid = true
                descriptionField.errorMessage = "Required"
            }
            if (validation.hasStandardFieldErrors) {
                standardFieldsPanel.setShowValidationErrors(true)
                tabs.selectedTab = standardTab
            }
            Notification.show(
                "Required: ${validation.missingFieldLabels.joinToString(", ")}",
                5000,
                Notification.Position.MIDDLE,
            ).addThemeVariants(NotificationVariant.LUMO_ERROR)
            return
        }

        try {
            val isNewTemplate = templateId == null
            val saved =
                if (isNewTemplate) {
                    productTemplateService.create(command)
                } else {
                    productTemplateService.update(templateId!!, command)
                }
            val message =
                if (isNewTemplate) {
                    "Created template: ${saved.name}"
                } else {
                    "Saved template: ${saved.name}"
                }
            Notification.show(message, 4000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            ui.ifPresent { ui ->
                if (isNewTemplate) {
                    ui.navigate(
                        TemplateListView::class.java,
                        QueryParameters.of(TemplateListView.HIGHLIGHT_QUERY_PARAM, saved.id),
                    )
                } else {
                    ui.navigate(TemplateListView::class.java)
                }
            }
        } catch (ex: ValidationException) {
            Notification.show(ex.message ?: "Validation failed", 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR)
        }
    }

    private fun confirmDelete() {
        val id = templateId ?: return
        ConfirmDialogs.confirmDelete(nameField.value) {
            try {
                productTemplateService.delete(id)
                Notification.show("Template deleted", 3000, Notification.Position.BOTTOM_START)
                ui.ifPresent { it.navigate(TemplateListView::class.java) }
            } catch (ex: ValidationException) {
                Notification.show(ex.message ?: "Delete failed", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR)
            }
        }
    }
}
