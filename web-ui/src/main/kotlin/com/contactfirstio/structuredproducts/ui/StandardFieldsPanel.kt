package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.data.document.FieldRequirement
import com.contactfirstio.structuredproducts.service.FieldCatalogService
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode

class StandardFieldsPanel(
    private val fieldCatalogService: FieldCatalogService,
    private val templateDefaultFieldFactory: TemplateDefaultFieldFactory,
    private val defaults: MutableMap<String, String>,
    private val caCaaDeclarationQuestions: MutableList<String>,
    private val templateId: String? = null,
) : VerticalLayout() {

    private val allFields = fieldCatalogService.standardFields()
    private val categoryOrder = fieldCatalogService.standardCategories()

    private val fixedValuesOnlyFilter =
        Checkbox("Fixed values only").apply {
            value = true
        }
    private val searchField =
        TextField().apply {
            placeholder = "Search fields..."
            prefixComponent = com.vaadin.flow.component.icon.VaadinIcon.SEARCH.create()
            isClearButtonVisible = true
            valueChangeMode = ValueChangeMode.EAGER
        }
    private val categoryFilter =
        ComboBox<String>().apply {
            placeholder = "Category"
            setItems(listOf("All") + categoryOrder)
            value = "All"
        }
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
            addColumn { it.dataType.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                .setHeader("Type")
                .setFlexGrow(0)
                .setWidth("5.5rem")
                .setAutoWidth(false)
            addComponentColumn { field ->
                Span(
                    if (field.requirement == FieldRequirement.MANDATORY) "Mandatory" else "Optional",
                ).apply {
                    addClassName("template-field-badge")
                    element.setAttribute(
                        "data-requirement",
                        field.requirement.name.lowercase(),
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
        hint.text =
            if (fixedValuesOnly) {
                "Set fixed values and CA/CAA declaration questions that will be locked on every product created from this template."
            } else {
                "All standard fields are included. Template values set here are applied when creating a product."
            }

        val filtered =
            allFields.filter { field ->
                val matchesCategory = category == "All" || field.category == category
                val matchesFixedValues =
                    !fixedValuesOnly ||
                        (field.defaultedInTemplateCreation && !field.excludedFromFixedValuesOnly)
                val matchesSearch =
                    query.isEmpty() ||
                        field.displayName.lowercase().contains(query) ||
                        field.key.lowercase().contains(query) ||
                        field.category.lowercase().contains(query)
                matchesCategory && matchesFixedValues && matchesSearch
            }

        content.removeAll()

        if (fixedValuesOnly) {
            content.add(
                TemplateFixedValuesForm(
                    categories = categoryOrder,
                    fields = filtered,
                    searchQuery = query,
                    editorFactory = templateDefaultFieldFactory,
                    defaults = defaults,
                    caCaaDeclarationQuestions = caCaaDeclarationQuestions,
                    templateId = templateId,
                    onStructureChange = { applyFilters() },
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
