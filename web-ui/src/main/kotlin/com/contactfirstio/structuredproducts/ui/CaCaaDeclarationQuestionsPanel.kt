package com.contactfirstio.structuredproducts.ui

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.data.value.ValueChangeMode

class CaCaaDeclarationQuestionsPanel(
    private val questions: MutableList<String>,
) : VerticalLayout() {

    private val questionsLayout =
        VerticalLayout().apply {
            addClassName("template-declaration-questions")
            isPadding = false
            setWidthFull()
            setSpacing(true)
        }

    init {
        addClassName("template-declaration-panel")
        isPadding = false
        setWidthFull()
        setSpacing(true)

        add(
            H2("CA/CAA declaration questions").apply {
                addClassName("template-declaration-panel-title")
            },
            Paragraph(
                "Write the declaration questions shown when creating a product from this template.",
            ).apply {
                addClassName("template-panel-hint")
            },
            questionsLayout,
            Button("Add question", VaadinIcon.PLUS.create()).apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClassName("template-add-declaration-button")
                addClickListener { addQuestion() }
            },
        )

        if (questions.isEmpty()) {
            addQuestion()
        } else {
            rebuildQuestions()
        }
    }

    fun questionCount(): Int = questions.count { it.isNotBlank() }

    private fun addQuestion() {
        val index = questions.size
        questions.add("")
        questionsLayout.add(questionRow(index))
        focusQuestion(index)
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
                    label = "Question ${index + 1}"
                    placeholder = "Enter declaration question"
                    width = "100%"
                    minHeight = "4rem"
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
                        if (questions.size <= 1) {
                            questions[0] = ""
                            questionField.clear()
                            return@addClickListener
                        }
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
    }

    companion object {
        const val CA_CAA_CATEGORY = "CA/CAA Declaration"
    }
}
