package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.service.FieldCatalogService
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.contactfirstio.structuredproducts.ui.DisplayFormatters
import com.vaadin.flow.data.value.ValueChangeMode

class StandardFieldsPanel(
    private val fieldCatalogService: FieldCatalogService,
    private val templateDefaultFieldFactory: TemplateDefaultFieldFactory,
    private val defaults: MutableMap<String, String>,
    private var templateId: String? = null,
) : VerticalLayout() {

    private var showValidationErrors = false

    private val allFields = fieldCatalogService.standardFields()
    private val categoryOrder = fieldCatalogService.standardCategories()

    private val fixedValuesOnlyFilter =
        Checkbox("Fixed values only").apply {
            value = true
        }
    private val searchField = FieldGridFilters.createSearchField(ValueChangeMode.LAZY)
    private val categoryFilter = FieldGridFilters.createCategoryFilter(categoryOrder)
    private val hint =
        Span().apply {
            addClassName("template-panel-hint")
        }
    private val toolbar =
        HorizontalLayout().apply {
            addClassName("template-fields-toolbar")
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
            isPadding = false
        }
    private val content = Div().apply {
        addClassName("template-fields-content")
        setWidthFull()
    }

    private val grid =
        Grid<CatalogFieldDefinition>().apply {
            addClassName("template-standard-grid")
            setWidthFull()
            isAllRowsVisible = true

            addColumn(CatalogFieldDefinition::displayName)
                .setHeader("Field")
                .setFlexGrow(2)
                .setTooltipGenerator(CatalogFieldDefinition::displayName)
            addColumn(CatalogFieldDefinition::category)
                .setHeader("Category")
                .setFlexGrow(1)
            addColumn { DisplayFormatters.snakeCaseLabel(it.dataType.name) }
                .setHeader("Type")
                .setFlexGrow(0)
                .setWidth("5.5rem")
                .setAutoWidth(false)
            addComponentColumn { field ->
                Span(
                    if (field.requiredInTemplateCreation) "Required" else "Optional",
                ).apply {
                    addClassName("template-field-badge")
                    element.setAttribute(
                        "data-requirement",
                        if (field.requiredInTemplateCreation) "required" else "optional",
                    )
                }
            }
                .setHeader("Rule")
                .setFlexGrow(0)
                .setWidth("7.5rem")
                .setAutoWidth(false)
            addComponentColumn { field ->
                templateDefaultFieldFactory.createEditor(
                    field = field,
                    currentValue = defaults[field.key],
                    templateId = templateId,
                    onValueChange = { newValue ->
                        updateDefault(field.key, newValue)
                    },
                    showValidationErrors = showValidationErrors,
                )
            }
                .setHeader("Fixed value")
                .setFlexGrow(0)
                .setWidth("12rem")
                .setAutoWidth(false)
        }

    init {
        addClassName("template-fields-panel")
        isPadding = false
        setWidthFull()

        toolbar.add(searchField, categoryFilter, fixedValuesOnlyFilter)

        fixedValuesOnlyFilter.addValueChangeListener { applyFilters() }
        searchField.addValueChangeListener { applyFilters() }
        categoryFilter.addValueChangeListener { applyFilters() }

        add(hint, toolbar, content)
        applyFilters()
    }

    fun setShowValidationErrors(show: Boolean) {
        showValidationErrors = show
        applyFilters()
    }

    fun setTemplateId(id: String?) {
        templateId = id
        applyFilters()
    }

    fun refresh() {
        applyFilters()
    }

    private fun updateDefault(key: String, newValue: String?) {
        if (newValue.isNullOrBlank()) {
            defaults.remove(key)
        } else {
            defaults[key] = newValue
        }
    }

    private fun applyFilters() {
        val query = searchField.value?.trim().orEmpty().lowercase()
        val category = categoryFilter.value ?: "All"
        val fixedValuesOnly = fixedValuesOnlyFilter.value

        categoryFilter.isVisible = !fixedValuesOnly
        hint.isVisible = !fixedValuesOnly
        hint.text = "All standard fields are included. Template values set here are applied when creating a product."

        val filtered =
            allFields.filter { field ->
                val matchesFilters =
                    FieldGridFilters.matchesCatalogField(field, query, category)
                val matchesFixedValues =
                    !fixedValuesOnly ||
                        (field.defaultedInTemplateCreation && !field.excludedFromFixedValuesOnly)
                matchesFilters && matchesFixedValues
            }

        content.removeAll()

        if (fixedValuesOnly) {
            content.add(
                TemplateFixedValuesForm(
                    categories = categoryOrder,
                    fields = filtered,
                    editorFactory = templateDefaultFieldFactory,
                    defaults = defaults,
                    templateId = templateId,
                    showValidationErrors = showValidationErrors,
                ),
            )
        } else {
            content.add(grid)
            if (filtered.isEmpty()) {
                grid.setItems(emptyList())
            } else {
                grid.setItems(
                    filtered.sortedWith(
                        compareBy<CatalogFieldDefinition> { it.category }.thenBy { it.displayName },
                    ),
                )
            }
        }
    }
}
