package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.catalog.StandardFieldCatalog
import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.FieldRequirement
import com.vaadin.flow.component.Component as VaadinComponent
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class TemplateFixedValuesForm(
    categories: List<String>,
    fields: List<CatalogFieldDefinition>,
    private val editorFactory: TemplateDefaultFieldFactory,
    private val defaults: MutableMap<String, String>,
    private val templateId: String?,
) : VerticalLayout() {

    init {
        addClassName("template-fixed-values-form")
        isPadding = false
        setWidthFull()
        setSpacing(false)

        val identifiersPanelFields =
            listOfNotNull(
                fields.find { it.key == APC_CODE_KEY },
                fields.find { it.key == PIP_ID_KEY },
            )
        val fieldsForCategories =
            fields.filter { field ->
                field.key != APC_CODE_KEY && field.key != PIP_ID_KEY
            }

        val fieldsByCategory =
            categories.mapNotNull { category ->
                val categoryFields =
                    sortFieldsForCategory(
                        category,
                        fieldsForCategories.filter { it.category == category },
                    )
                if (categoryFields.isEmpty()) null else category to categoryFields
            }

        if (fieldsByCategory.isEmpty() && identifiersPanelFields.isEmpty()) {
            add(
                Span("No fixed value fields match your search.").apply {
                    addClassName("template-empty-state")
                },
            )
        } else {
            fieldsByCategory.forEach { (category, categoryFields) ->
                if (category == StandardFieldCatalog.CATEGORY_INITIAL_SETUP) {
                    add(buildInitialSetupRow(categoryFields, identifiersPanelFields))
                } else {
                    add(buildCategorySection(category, categoryFields))
                }
            }
            if (
                identifiersPanelFields.isNotEmpty() &&
                    fieldsByCategory.none { it.first == StandardFieldCatalog.CATEGORY_INITIAL_SETUP }
            ) {
                add(buildIdentifiersPanel(identifiersPanelFields))
            }
        }
    }

    private fun buildInitialSetupRow(
        setupFields: List<CatalogFieldDefinition>,
        identifiersPanelFields: List<CatalogFieldDefinition>,
    ): VaadinComponent =
        if (identifiersPanelFields.isEmpty()) {
            buildCategorySection(StandardFieldCatalog.CATEGORY_INITIAL_SETUP, setupFields)
        } else {
            HorizontalLayout().apply {
                addClassName("template-fixed-values-initial-setup-row")
                setWidthFull()
                isPadding = false
                setSpacing(true)
                setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH)

                add(
                    buildCategorySection(StandardFieldCatalog.CATEGORY_INITIAL_SETUP, setupFields).apply {
                        style.set("flex", "1 1 20rem")
                        style.set("min-width", "0")
                    },
                    buildIdentifiersPanel(identifiersPanelFields),
                )
            }
        }

    private fun buildIdentifiersPanel(panelFields: List<CatalogFieldDefinition>): Div =
        Div().apply {
            addClassName("template-fixed-values-section")
            addClassName("template-fixed-values-apc-panel")
            add(H3(StandardFieldCatalog.CATEGORY_IDENTIFIERS_AND_REGULATORY).apply {
                addClassName("template-fixed-values-section-title")
            })
            add(buildCategoryForm(panelFields))
        }

    private fun buildCategorySection(
        category: String,
        categoryFields: List<CatalogFieldDefinition>,
    ): Div =
        Div().apply {
            addClassName("template-fixed-values-section")
            add(H3(category).apply { addClassName("template-fixed-values-section-title") })
            add(buildCategoryForm(categoryFields))
        }

    private fun buildCategoryForm(categoryFields: List<CatalogFieldDefinition>): FormLayout =
        FormLayout().apply {
            addClassName("template-fixed-values-layout")
            setWidthFull()
            setResponsiveSteps(
                FormLayout.ResponsiveStep("0", 1),
                FormLayout.ResponsiveStep("36rem", 2),
                FormLayout.ResponsiveStep("60rem", 3),
            )

            layoutCategoryFields(categoryFields).forEach { entry ->
                when (entry) {
                    is CategoryFormEntry.Single -> addField(this, entry.field)
                    is CategoryFormEntry.EffectiveDatePair ->
                        addEffectiveDatePair(this, entry.from, entry.to)
                }
            }
        }

    private fun layoutCategoryFields(
        categoryFields: List<CatalogFieldDefinition>,
    ): List<CategoryFormEntry> {
        val hasEffectiveFrom = categoryFields.any { it.key == EFFECTIVE_FROM_KEY }
        val hasEffectiveTo = categoryFields.any { it.key == EFFECTIVE_TO_KEY }
        val pairEffectiveDates = hasEffectiveFrom && hasEffectiveTo

        return categoryFields.mapNotNull { field ->
            when {
                pairEffectiveDates && field.key == EFFECTIVE_TO_KEY -> null
                pairEffectiveDates && field.key == EFFECTIVE_FROM_KEY -> {
                    val toField = categoryFields.first { it.key == EFFECTIVE_TO_KEY }
                    CategoryFormEntry.EffectiveDatePair(field, toField)
                }
                else -> CategoryFormEntry.Single(field)
            }
        }
    }

    private fun addField(
        form: FormLayout,
        field: CatalogFieldDefinition,
    ) {
        val editor = createFieldEditor(field)
        form.addFormItem(editor, fieldLabel(field))
        form.setColspan(editor, fieldColspan(field))
    }

    private fun addEffectiveDatePair(
        form: FormLayout,
        fromField: CatalogFieldDefinition,
        toField: CatalogFieldDefinition,
    ) {
        val row = buildEffectiveDatePair(fromField, toField)
        form.addFormItem(row, Span().apply { isVisible = false })
        form.setColspan(row, 3)
    }

    private fun buildEffectiveDatePair(
        fromField: CatalogFieldDefinition,
        toField: CatalogFieldDefinition,
    ): HorizontalLayout {
        val fromColumn = buildLabeledField(fromField)
        val toColumn = buildLabeledField(toField)
        return HorizontalLayout(fromColumn, toColumn).apply {
            addClassName("template-effective-date-row")
            setWidthFull()
            isPadding = false
            setSpacing(true)
            setFlexGrow(1.0, fromColumn, toColumn)
        }
    }

    private fun buildLabeledField(field: CatalogFieldDefinition): VerticalLayout =
        VerticalLayout().apply {
            isPadding = false
            setSpacing(false)
            style.set("min-width", "0")
            add(
                Span(fieldLabel(field)).apply { addClassName("template-form-field-label") },
                createFieldEditor(field),
            )
        }

    private fun createFieldEditor(field: CatalogFieldDefinition): VaadinComponent =
        editorFactory.createEditor(
            field = field,
            currentValue = defaults[field.key],
            templateId = templateId,
            onValueChange = { newValue ->
                if (newValue.isNullOrBlank()) {
                    defaults.remove(field.key)
                } else {
                    defaults[field.key] = newValue
                }
            },
            formLayout = true,
        )

    private fun fieldLabel(field: CatalogFieldDefinition): String =
        if (field.requirement == FieldRequirement.MANDATORY) {
            "${field.displayName} *"
        } else {
            field.displayName
        }

    private fun fieldColspan(field: CatalogFieldDefinition): Int =
        when {
            field.dataType == FieldDataType.FILE_LIST -> 3
            field.dataType == FieldDataType.CHECKBOX_GROUP -> 2
            field.dataType == FieldDataType.DATETIME -> 2
            field.key == "pay_off" || field.key == "brochure_link" -> 3
            else -> 1
        }

    private fun sortFieldsForCategory(
        category: String,
        categoryFields: List<CatalogFieldDefinition>,
    ): List<CatalogFieldDefinition> {
        if (category != StandardFieldCatalog.CATEGORY_PRODUCT_LIFECYCLE) {
            return categoryFields
        }

        val order = StandardFieldCatalog.productLifecycleFixedValueFieldOrder
        return categoryFields.sortedBy { field ->
            val index = order.indexOf(field.key)
            if (index == -1) Int.MAX_VALUE else index
        }
    }

    private companion object {
        const val APC_CODE_KEY = "apc_code"
        const val PIP_ID_KEY = "pip_id"
        const val EFFECTIVE_FROM_KEY = "effective_from"
        const val EFFECTIVE_TO_KEY = "effective_to"
    }

    private sealed interface CategoryFormEntry {
        data class Single(val field: CatalogFieldDefinition) : CategoryFormEntry

        data class EffectiveDatePair(
            val from: CatalogFieldDefinition,
            val to: CatalogFieldDefinition,
        ) : CategoryFormEntry
    }
}
