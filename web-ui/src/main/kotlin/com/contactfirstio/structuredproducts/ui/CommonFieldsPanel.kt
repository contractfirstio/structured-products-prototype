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
) : VerticalLayout() {

    private data class CommonFieldRow(
        val field: CatalogFieldDefinition,
        var included: Boolean,
    )

    private val allFields = fieldCatalogService.commonFields()
    private val rows = allFields.map { CommonFieldRow(it, includedKeys.contains(it.key)) }.toMutableList()
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
            height = "360px"

            addComponentColumn { row ->
                Checkbox(row.included).apply {
                    addValueChangeListener { event ->
                        row.included = event.value
                        if (event.value) {
                            includedKeys.add(row.field.key)
                        } else {
                            includedKeys.remove(row.field.key)
                        }
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
                Span(
                    if (row.field.requirement == FieldRequirement.MANDATORY) "Mandatory" else "Optional",
                ).apply {
                    addClassName("template-field-badge")
                    element.setAttribute(
                        "data-requirement",
                        row.field.requirement.name.lowercase(),
                    )
                }
            }
                .setHeader("Rule")
                .setFlexGrow(0)
                .setWidth("7.5rem")
                .setAutoWidth(false)
        }

    init {
        addClassName("template-fields-panel")
        isPadding = false
        setWidthFull()

        searchField.addValueChangeListener { applyFilters() }
        categoryFilter.addValueChangeListener { applyFilters() }
        showFilter.addValueChangeListener { applyFilters() }

        add(
            Span(
                "Only included product specific fields appear when creating a product from this template.",
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
                Span("No product specific fields are defined in the catalog yet.").apply {
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
        rows.forEach { row -> row.included = includedKeys.contains(row.field.key) }
        applyFilters()
    }
}
