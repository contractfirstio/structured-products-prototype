package com.contactfirstio.structuredproducts.ui

class CaCaaDeclarationQuestionsPanel(
    questions: MutableList<String>,
) {
    private val panel =
        EditableStringListPanel(
            items = questions,
            title = "CA/CAA declaration questions",
            titleClassName = "template-declaration-panel-title",
            hint = "Write the declaration questions shown when creating a product from this template.",
            itemLabel = { index -> "Question ${index + 1}" },
            itemPlaceholder = "Enter declaration question",
            addButtonLabel = "Add question",
            addButtonClassName = "template-add-declaration-button",
            listClassName = "template-declaration-questions",
            rowClassName = "template-declaration-question-editor",
            panelClassName = "template-declaration-panel",
        )

    val component: EditableStringListPanel get() = panel

    fun questionCount(): Int = panel.nonBlankCount()

    fun refresh() = panel.refresh()

    companion object {
        const val CA_CAA_CATEGORY = "CA/CAA Declaration"
    }
}
