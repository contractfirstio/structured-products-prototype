package com.contactfirstio.structuredproducts.ui

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.data.value.ValueChangeMode

class CaCaaDeclarationQuestionsPanel(
    private val questions: MutableList<String>,
) : VerticalLayout() {

    private val emptyState =
        Span("No declaration questions yet.").apply {
            addClassName("template-empty-state")
        }
    private val questionsLayout =
        VerticalLayout().apply {
            addClassName("template-declaration-questions")
            isPadding = false
            setWidthFull()
            setSpacing(true)
        }

    init {
        addClassName("template-fixed-values-section")
        isPadding = false
        setWidthFull()
        setSpacing(true)

        add(
            H3(CA_CAA_CATEGORY).apply {
                addClassName("template-fixed-values-section-title")
            },
            emptyState,
            questionsLayout,
            Button("Add question", VaadinIcon.PLUS.create()).apply {
                addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                addClassName("template-add-declaration-button")
                addClickListener { addQuestion() }
            },
        )

        syncEmptyState()
        questions.forEachIndexed { index, _ ->
            questionsLayout.add(questionRow(index))
        }
    }

    private fun addQuestion() {
        val index = questions.size
        questions.add("")
        questionsLayout.add(questionRow(index))
        syncEmptyState()
        focusQuestion(index)
    }

    private fun syncEmptyState() {
        emptyState.isVisible = questions.isEmpty()
        questionsLayout.isVisible = questions.isNotEmpty()
    }

    private fun focusQuestion(index: Int) {
        questionsLayout.children
            .skip(index.toLong())
            .findFirst()
            .ifPresent { row ->
                row.children
                    .filter { it is TextArea }
                    .findFirst()
                    .ifPresent { it.element.executeJs("this.focus()") }
            }
    }

    private fun questionRow(index: Int): HorizontalLayout =
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
                    value = questions.getOrElse(index) { "" }
                    valueChangeMode = ValueChangeMode.EAGER
                    addValueChangeListener { event ->
                        questions[index] = event.value.orEmpty()
                    }
                }

            add(questionField)
            expand(questionField)

            add(
                Button(VaadinIcon.TRASH.create()).apply {
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR)
                    element.setAttribute("aria-label", "Remove question ${index + 1}")
                    addClickListener {
                        questions.removeAt(index)
                        rebuildQuestions()
                    }
                },
            )
        }

    private fun rebuildQuestions() {
        questionsLayout.removeAll()
        questions.indices.forEach { index ->
            questionsLayout.add(questionRow(index))
        }
        syncEmptyState()
    }

    companion object {
        const val CA_CAA_CATEGORY = "CA/CAA Declaration"
    }
}
