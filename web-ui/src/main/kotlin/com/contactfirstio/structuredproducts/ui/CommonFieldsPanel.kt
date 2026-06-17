package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.data.document.FieldRequirement
import com.contactfirstio.structuredproducts.service.FieldCatalogService
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode

class CommonFieldsPanel(
    fieldCatalogService: FieldCatalogService,
    private val includedKeys: MutableSet<String>,
    private val mandatoryKeys: MutableSet<String>,
) : VerticalLayout() {

    private data class CommonFieldRow(
        val field: CatalogFieldDefinition,
        var included: Boolean,
        var requirement: FieldRequirement,
    )

    private val allFields = fieldCatalogService.commonFields()
    private val rows =
        allFields
            .map { field ->
                CommonFieldRow(
                    field = field,
                    included = includedKeys.contains(field.key),
                    requirement =
                        if (mandatoryKeys.contains(field.key)) {
                            FieldRequirement.MANDATORY
                        } else {
                            FieldRequirement.OPTIONAL
                        },
                )
            }
            .toMutableList()
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
            setItems(listOf("All") + fieldCatalogService.commonCategories())
            value = "All"
        }
    private val showFilter =
        ComboBox<String>().apply {
            placeholder = "Show"
            setItems("All", "Included only", "Not included")
            value = "All"
        }
    private val grid =
        Grid<CommonFieldRow>().apply {
            addClassName("template-common-grid")
            setWidthFull()

            addComponentColumn { row ->
                Checkbox(row.included).apply {
                    addValueChangeListener { event ->
                        row.included = event.value
                        if (event.value) {
                            includedKeys.add(row.field.key)
                        } else {
                            includedKeys.remove(row.field.key)
                            row.requirement = FieldRequirement.OPTIONAL
                            mandatoryKeys.remove(row.field.key)
                        }
                        applyFilters()
                    }
                }
            }
                .setHeader("Include")
                .setFlexGrow(0)
                .setWidth("100px")

            addColumn { it.field.displayName }.setHeader("Field").setFlexGrow(2)
            addColumn { it.field.category }.setHeader("Category").setFlexGrow(1)
            addColumn { it.field.dataType.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                .setHeader("Type")
                .setFlexGrow(0)
                .setWidth("5.5rem")
                .setAutoWidth(false)
            addComponentColumn { row ->
                ComboBox<FieldRequirement>().apply {
                    addClassName("template-common-requirement")
                    setItems(FieldRequirement.OPTIONAL, FieldRequirement.MANDATORY)
                    setItemLabelGenerator {
                        if (it == FieldRequirement.MANDATORY) "Mandatory" else "Optional"
                    }
                    value = row.requirement
                    isEnabled = row.included
                    width = "7.5rem"
                    addValueChangeListener { event ->
                        val selected = event.value ?: FieldRequirement.OPTIONAL
                        row.requirement = selected
                        if (selected == FieldRequirement.MANDATORY) {
                            mandatoryKeys.add(row.field.key)
                        } else {
                            mandatoryKeys.remove(row.field.key)
                        }
                    }
                }
            }
                .setHeader("Rule")
                .setFlexGrow(0)
                .setWidth("8.5rem")
                .setAutoWidth(false)
        }

    init {
        addClassName("template-fields-panel")
        addClassName("template-common-fields-panel")
        isPadding = false
        setWidthFull()

        searchField.addValueChangeListener { applyFilters() }
        categoryFilter.addValueChangeListener { applyFilters() }
        showFilter.addValueChangeListener { applyFilters() }

        add(
            Span(
                "Choose which common fields appear when creating a product. " +
                    "Included fields default to optional; set Rule to Mandatory when required.",
            ).apply {
                addClassName("template-panel-hint")
            },
            HorizontalLayout(searchField, categoryFilter, showFilter).apply {
                addClassName("template-fields-toolbar")
                setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
                isPadding = false
            },
        )

        if (allFields.isEmpty()) {
            add(
                Span("No common fields are defined in the catalog yet.").apply {
                    addClassName("template-empty-state")
                },
            )
        } else {
            add(grid)
            applyFilters()
        }
    }

    private fun applyFilters() {
        val query = searchField.value?.trim().orEmpty().lowercase()
        val category = categoryFilter.value ?: "All"
        val show = showFilter.value ?: "All"

        val filtered =
            rows.filter { row ->
                val matchesCategory = category == "All" || row.field.category == category
                val matchesShow =
                    when (show) {
                        "Included only" -> row.included
                        "Not included" -> !row.included
                        else -> true
                    }
                val matchesSearch =
                    query.isEmpty() ||
                        row.field.displayName.lowercase().contains(query) ||
                        row.field.category.lowercase().contains(query)
                matchesCategory && matchesShow && matchesSearch
            }

        grid.setItems(filtered.sortedWith(compareByDescending<CommonFieldRow> { it.included }.thenBy { it.field.displayName }))
    }

    fun syncFromIncludedKeys() {
        rows.forEach { row ->
            row.included = includedKeys.contains(row.field.key)
            row.requirement =
                if (mandatoryKeys.contains(row.field.key)) {
                    FieldRequirement.MANDATORY
                } else {
                    FieldRequirement.OPTIONAL
                }
        }
        applyFilters()
    }
}
