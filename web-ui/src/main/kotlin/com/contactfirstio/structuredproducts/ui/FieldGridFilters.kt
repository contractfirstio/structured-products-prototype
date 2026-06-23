package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode

object FieldGridFilters {
    fun createSearchField(valueChangeMode: ValueChangeMode = ValueChangeMode.LAZY): TextField =
        TextField().apply {
            placeholder = "Search fields..."
            prefixComponent = VaadinIcon.SEARCH.create()
            isClearButtonVisible = true
            this.valueChangeMode = valueChangeMode
        }

    fun createCategoryFilter(categories: List<String>): ComboBox<String> =
        ComboBox<String>().apply {
            placeholder = "Category"
            setItems(listOf("All") + categories)
            value = "All"
        }

    fun matchesCatalogField(
        field: CatalogFieldDefinition,
        query: String,
        category: String,
    ): Boolean {
        val normalizedQuery = query.trim().lowercase()
        val matchesCategory = category == "All" || field.category == category
        val matchesSearch =
            normalizedQuery.isEmpty() ||
                field.displayName.lowercase().contains(normalizedQuery) ||
                field.key.lowercase().contains(normalizedQuery) ||
                field.category.lowercase().contains(normalizedQuery)
        return matchesCategory && matchesSearch
    }
}
