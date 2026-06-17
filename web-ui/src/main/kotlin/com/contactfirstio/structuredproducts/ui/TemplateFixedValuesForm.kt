package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.FieldRequirement
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.data.value.ValueChangeMode

class TemplateFixedValuesForm(
    categories: List<String>,
    fields: List<CatalogFieldDefinition>,
    searchQuery: String,
    private val editorFactory: TemplateDefaultFieldFactory,
    private val defaults: MutableMap<String, String>,
    private val caCaaDeclarationQuestions: MutableList<String>,
    private val templateId: String?,
    private val onStructureChange: () -> Unit,
) : VerticalLayout() {

    init {
        addClassName("template-fixed-values-form")
        isPadding = false
        setWidthFull()
        setSpacing(false)

        val fieldsByCategory =
            categories.mapNotNull { category ->
                val categoryFields = fields.filter { it.category == category }
                if (categoryFields.isEmpty()) null else category to categoryFields
            }

        val showCaCaa = matchesCaCaaSearch(searchQuery)
        val visibleQuestions = visibleCaCaaQuestionIndices(searchQuery)

        if (fieldsByCategory.isEmpty() && (!showCaCaa || visibleQuestions.isEmpty())) {
            add(
                Span("No fixed value fields match your search.").apply {
                    addClassName("template-empty-state")
                },
            )
        } else {
            fieldsByCategory.forEach { (category, categoryFields) ->
                add(buildCategorySection(category, categoryFields))
            }

            if (showCaCaa) {
                add(buildCaCaaSection(visibleQuestions))
            }
        }
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

            categoryFields.forEach { field ->
                val editor =
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
                addFormItem(editor, fieldLabel(field))
                setColspan(editor, fieldColspan(field))
            }
        }

    private fun buildCaCaaSection(visibleQuestionIndices: List<Int>): Div =
        Div().apply {
            addClassName("template-fixed-values-section")
            add(
                H3(CA_CAA_CATEGORY).apply {
                    addClassName("template-fixed-values-section-title")
                },
            )

            val questionsLayout =
                VerticalLayout().apply {
                    addClassName("template-declaration-questions")
                    isPadding = false
                    setWidthFull()
                    setSpacing(true)
                }

            if (visibleQuestionIndices.isEmpty()) {
                questionsLayout.add(
                    Span("No declaration questions added yet.").apply {
                        addClassName("template-field-placeholder")
                    },
                )
            } else {
                visibleQuestionIndices.forEach { index ->
                    questionsLayout.add(declarationQuestionRow(index))
                }
            }

            add(questionsLayout)
            add(
                Button("Add declaration question", VaadinIcon.PLUS.create()) {
                    caCaaDeclarationQuestions.add("")
                    onStructureChange()
                }.apply {
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE)
                    addClassName("template-add-declaration-button")
                },
            )
        }

    private fun declarationQuestionRow(index: Int): HorizontalLayout =
        HorizontalLayout().apply {
            addClassName("template-declaration-question-editor")
            setWidthFull()
            isPadding = false
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START)

            val questionField =
                TextArea().apply {
                    addClassName("template-form-editor")
                    placeholder = "Enter declaration question"
                    width = "100%"
                    minHeight = "3.25rem"
                    value = caCaaDeclarationQuestions.getOrElse(index) { "" }
                    valueChangeMode = ValueChangeMode.EAGER
                    addValueChangeListener { event ->
                        caCaaDeclarationQuestions[index] = event.value.orEmpty()
                    }
                }
            add(questionField)
            expand(questionField)

            add(
                Button(VaadinIcon.TRASH.create()) {
                    caCaaDeclarationQuestions.removeAt(index)
                    onStructureChange()
                }.apply {
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR)
                    element.setAttribute("aria-label", "Remove question ${index + 1}")
                },
            )
        }

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

    private fun visibleCaCaaQuestionIndices(searchQuery: String): List<Int> {
        if (!matchesCaCaaSearch(searchQuery)) return emptyList()

        val query = searchQuery.lowercase()
        val showAllQuestions =
            searchQuery.isBlank() ||
                CA_CAA_CATEGORY.lowercase().contains(query) ||
                query.contains("caa") ||
                query.contains("declaration")

        return caCaaDeclarationQuestions.indices.filter { index ->
            val question = caCaaDeclarationQuestions[index]
            showAllQuestions || question.lowercase().contains(query)
        }
    }

    private fun matchesCaCaaSearch(searchQuery: String): Boolean {
        if (searchQuery.isBlank()) return true
        val query = searchQuery.lowercase()
        return CA_CAA_CATEGORY.lowercase().contains(query) ||
            query.contains("caa") ||
            query.contains("declaration") ||
            caCaaDeclarationQuestions.any { it.lowercase().contains(query) }
    }

    companion object {
        const val CA_CAA_CATEGORY = "CA/CAA Declaration"
    }
}
