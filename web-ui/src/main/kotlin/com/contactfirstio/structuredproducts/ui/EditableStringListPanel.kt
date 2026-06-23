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

class EditableStringListPanel(
    private val items: MutableList<String>,
    title: String,
    titleClassName: String,
    hint: String,
    private val itemLabel: (Int) -> String,
    private val itemPlaceholder: String,
    addButtonLabel: String,
    addButtonClassName: String,
    listClassName: String,
    private val rowClassName: String,
    panelClassName: String,
) : VerticalLayout() {

    private val itemsLayout =
        VerticalLayout().apply {
            addClassName(listClassName)
            isPadding = false
            setWidthFull()
            setSpacing(true)
        }

    init {
        addClassName(panelClassName)
        isPadding = false
        setWidthFull()
        setSpacing(true)

        add(
            H2(title).apply {
                addClassName(titleClassName)
            },
            Paragraph(hint).apply {
                addClassName("template-panel-hint")
            },
            itemsLayout,
            Button(addButtonLabel, VaadinIcon.PLUS.create()).apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClassName(addButtonClassName)
                addClickListener { addItem() }
            },
        )

        if (items.isEmpty()) {
            addItem()
        } else {
            rebuildItems()
        }
    }

    fun nonBlankCount(): Int = items.count { it.isNotBlank() }

    fun refresh() {
        if (items.isEmpty()) {
            items.add("")
        }
        rebuildItems()
    }

    private fun addItem() {
        val index = items.size
        items.add("")
        itemsLayout.add(itemRow(index))
        focusItem(index)
    }

    private fun focusItem(index: Int) {
        itemsLayout.children
            .skip(index.toLong())
            .findFirst()
            .ifPresent { row ->
                row.children
                    .filter { it is TextArea }
                    .findFirst()
                    .ifPresent { it.element.executeJs("this.focus()") }
            }
    }

    private fun rebuildItems() {
        itemsLayout.removeAll()
        items.indices.forEach { index ->
            itemsLayout.add(itemRow(index))
        }
    }

    private fun itemRow(index: Int): HorizontalLayout =
        HorizontalLayout().apply {
            addClassName(rowClassName)
            setWidthFull()
            isPadding = false
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START)

            val itemField =
                TextArea().apply {
                    addClassName("template-form-editor")
                    label = itemLabel(index)
                    placeholder = itemPlaceholder
                    width = "100%"
                    minHeight = "4rem"
                    value = items.getOrElse(index) { "" }
                    valueChangeMode = ValueChangeMode.EAGER
                    addValueChangeListener { event ->
                        items[index] = event.value.orEmpty()
                    }
                }

            add(itemField)
            expand(itemField)

            add(
                Button(VaadinIcon.TRASH.create()).apply {
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR)
                    element.setAttribute("aria-label", "Remove ${itemLabel(index)}")
                    addClickListener {
                        if (items.size <= 1) {
                            items[0] = ""
                            itemField.clear()
                            return@addClickListener
                        }
                        items.removeAt(index)
                        rebuildItems()
                    }
                },
            )
        }
}
