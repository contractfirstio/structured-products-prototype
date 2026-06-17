package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CaCaaDeclarationQuestion
import com.contactfirstio.structuredproducts.service.CaCaaDeclarationCatalogService
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode

class CaCaaDeclarationPanel(
    caCaaDeclarationCatalogService: CaCaaDeclarationCatalogService,
    private val includedQuestionKeys: MutableSet<String>,
) : VerticalLayout() {

    private data class QuestionRow(
        val question: CaCaaDeclarationQuestion,
        var included: Boolean,
    )

    private val allQuestions = caCaaDeclarationCatalogService.questions()
    private val rows =
        allQuestions
            .map { QuestionRow(it, includedQuestionKeys.contains(it.key)) }
            .toMutableList()
    private val searchField =
        TextField().apply {
            placeholder = "Search questions..."
            prefixComponent = com.vaadin.flow.component.icon.VaadinIcon.SEARCH.create()
            isClearButtonVisible = true
            valueChangeMode = ValueChangeMode.EAGER
        }
    private val showFilter =
        ComboBox<String>().apply {
            placeholder = "Show"
            setItems("All", "Included only", "Not included")
            value = "All"
        }
    private val grid =
        Grid<QuestionRow>().apply {
            addClassName("template-declaration-grid")
            setWidthFull()
            isAllRowsVisible = true

            addComponentColumn { row ->
                Checkbox(row.included).apply {
                    addValueChangeListener { event ->
                        row.included = event.value
                        if (event.value) {
                            includedQuestionKeys.add(row.question.key)
                        } else {
                            includedQuestionKeys.remove(row.question.key)
                        }
                    }
                }
            }
                .setHeader("Include")
                .setFlexGrow(0)
                .setWidth("100px")

            addColumn { it.question.question }
                .setHeader("Question")
                .setFlexGrow(2)
                .setTooltipGenerator { it.question.question }
        }

    init {
        addClassName("template-fields-panel")
        isPadding = false
        setWidthFull()

        searchField.addValueChangeListener { applyFilters() }
        showFilter.addValueChangeListener { applyFilters() }

        add(
            Span(
                "Select CA/CAA declaration questions to include when creating a product from this template.",
            ).apply {
                addClassName("template-panel-hint")
            },
            HorizontalLayout(searchField, showFilter).apply {
                addClassName("template-fields-toolbar")
                setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
                isPadding = false
            },
            grid,
        )

        applyFilters()
    }

    private fun applyFilters() {
        val query = searchField.value?.trim().orEmpty().lowercase()
        val show = showFilter.value ?: "All"

        val filtered =
            rows.filter { row ->
                val matchesShow =
                    when (show) {
                        "Included only" -> row.included
                        "Not included" -> !row.included
                        else -> true
                    }
                val matchesSearch =
                    query.isEmpty() ||
                        row.question.question.lowercase().contains(query) ||
                        row.question.key.lowercase().contains(query)
                matchesShow && matchesSearch
            }

        grid.setItems(
            filtered.sortedWith(
                compareByDescending<QuestionRow> { it.included }.thenBy { it.question.question },
            ),
        )
    }
}
