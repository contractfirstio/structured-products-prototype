package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.service.FieldCatalogService
import com.contactfirstio.structuredproducts.service.ProductTemplateDetail
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.details.Details
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TemplateDetailPanel(
    private val fieldCatalogService: FieldCatalogService,
    private val onEdit: (String) -> Unit,
) : VerticalLayout() {

    private val contentArea =
        VerticalLayout().apply {
            addClassName("template-detail-panel-content")
            isPadding = false
            setWidthFull()
            setSpacing(true)
        }

    private val emptyState =
        Span("Select a template from the list to preview its configuration.").apply {
            addClassName("template-detail-empty")
        }

    init {
        addClassName("template-detail-panel-inner")
        isPadding = false
        setWidthFull()
        setSpacing(false)
        add(contentArea)
        showEmpty()
    }

    fun showEmpty() {
        contentArea.removeAll()
        contentArea.add(emptyState)
    }

    fun showDetail(detail: ProductTemplateDetail) {
        contentArea.removeAll()

        val statusLabel = detail.status.name.lowercase().replaceFirstChar { it.uppercase() }
        val updatedLabel =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(detail.updatedAt)

        val nameHeading = H3(detail.name).apply { addClassName("template-detail-name") }
        val statusBadge =
            Span(statusLabel).apply {
                addClassNames("template-field-badge", "template-detail-status")
                element.setAttribute("data-status", detail.status.name.lowercase())
            }
        val updatedMeta = Span("Updated $updatedLabel").apply { addClassName("template-detail-meta") }
        val editButton =
            Button("Edit", VaadinIcon.EDIT.create()) { onEdit(detail.id) }.apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL)
            }

        val header =
            VerticalLayout().apply {
                addClassName("template-detail-header")
                isPadding = false
                setSpacing(true)
                setWidthFull()

                add(
                    HorizontalLayout(nameHeading, statusBadge).apply {
                        addClassName("template-detail-title-row")
                        setWidthFull()
                        isPadding = false
                        setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
                        expand(nameHeading)
                    },
                )

                if (detail.description.isNotBlank()) {
                    add(
                        Paragraph(detail.description).apply {
                            addClassName("template-detail-description")
                        },
                    )
                }

                add(
                    HorizontalLayout(updatedMeta, editButton).apply {
                        addClassName("template-detail-actions")
                        setWidthFull()
                        isPadding = false
                        setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
                        expand(updatedMeta)
                    },
                )
            }

        val standardFields = buildStandardFields(detail)
        val commonFields = buildCommonFields(detail)
        val caCaaQuestions = detail.caCaaDeclarationQuestions.filter { it.isNotBlank() }
        val customFields = detail.customFields.filter { it.label.isNotBlank() || it.key.isNotBlank() }

        val sections =
            VerticalLayout().apply {
                addClassName("template-detail-sections")
                isPadding = false
                setWidthFull()
                setSpacing(true)

                add(
                    buildSection(
                        summary = "Standard fields (${standardFields.size})",
                        opened = true,
                        body = standardFieldsSection(standardFields),
                    ),
                    buildSection(
                        summary = "Common fields (${commonFields.size})",
                        opened = false,
                        body = commonFieldsSection(commonFields),
                    ),
                    buildSection(
                        summary = "CA/CAA declaration (${caCaaQuestions.size})",
                        opened = false,
                        body = caCaaSection(caCaaQuestions),
                    ),
                    buildSection(
                        summary = "Custom fields (${customFields.size})",
                        opened = false,
                        body = customFieldsSection(customFields),
                    ),
                )
            }

        contentArea.add(header, sections)
    }

    private fun buildSection(
        summary: String,
        opened: Boolean,
        body: Div,
    ): Details =
        Details(summary, body).apply {
            addClassName("template-detail-section")
            isOpened = opened
        }

    private fun buildStandardFields(detail: ProductTemplateDetail): List<StandardFieldDisplay> =
        fieldCatalogService.standardFields()
            .sortedWith(compareBy<CatalogFieldDefinition> { it.category }.thenBy { it.displayName })
            .map { field ->
                val (value, placeholder) = formatStandardFieldValue(field, detail.standardFieldDefaults[field.key])
                StandardFieldDisplay(field.displayName, value, placeholder)
            }

    private fun formatStandardFieldValue(
        field: CatalogFieldDefinition,
        rawValue: String?,
    ): Pair<String, Boolean> =
        when {
            field.systemManagedAtProductCreation -> "Set by system" to true
            !field.defaultedInTemplateCreation -> "Set at product creation" to true
            !rawValue.isNullOrBlank() -> rawValue to false
            else -> "Not set" to true
        }

    private fun buildCommonFields(detail: ProductTemplateDetail): List<Pair<String, Boolean>> =
        fieldCatalogService.commonFields()
            .filter { it.key in detail.includedCommonFieldKeys }
            .sortedBy { it.displayName }
            .map { field ->
                field.displayName to (field.key in detail.mandatoryCommonFieldKeys)
            }

    private fun standardFieldsSection(entries: List<StandardFieldDisplay>): Div =
        Div().apply {
            addClassName("template-detail-section-body")
            if (entries.isEmpty()) {
                add(emptySectionMessage("No standard fields configured."))
            } else {
                entries.forEach { entry ->
                    add(keyValueRow(entry.label, entry.value, entry.placeholder))
                }
            }
        }

    private fun commonFieldsSection(entries: List<Pair<String, Boolean>>): Div =
        Div().apply {
            addClassName("template-detail-section-body")
            if (entries.isEmpty()) {
                add(emptySectionMessage("No common fields included."))
            } else {
                entries.forEach { (label, mandatory) ->
                    add(
                        Div().apply {
                            addClassName("template-detail-list-item")
                            add(
                                Span(label).apply { addClassName("template-detail-list-label") },
                                Span(if (mandatory) "Mandatory" else "Optional").apply {
                                    addClassName("template-field-badge")
                                    element.setAttribute(
                                        "data-requirement",
                                        if (mandatory) "mandatory" else "optional",
                                    )
                                },
                            )
                        },
                    )
                }
            }
        }

    private fun caCaaSection(questions: List<String>): Div =
        Div().apply {
            addClassName("template-detail-section-body")
            if (questions.isEmpty()) {
                add(emptySectionMessage("No declaration questions."))
            } else {
                questions.forEachIndexed { index, question ->
                    add(
                        Div().apply {
                            addClassName("template-detail-question")
                            add(
                                Span("${index + 1}.").apply { addClassName("template-detail-question-index") },
                                Span(question).apply { addClassName("template-detail-question-text") },
                            )
                        },
                    )
                }
            }
        }

    private fun customFieldsSection(fields: List<com.contactfirstio.structuredproducts.data.document.TemplateCustomFieldDefinition>): Div =
        Div().apply {
            addClassName("template-detail-section-body")
            if (fields.isEmpty()) {
                add(emptySectionMessage("No custom fields."))
            } else {
                fields.forEach { field ->
                    val typeLabel =
                        field.dataType.name.lowercase().replace('_', ' ')
                            .replaceFirstChar { it.uppercase() }
                    add(
                        Div().apply {
                            addClassName("template-detail-custom-field")
                            add(
                                Span(field.label.ifBlank { field.key }).apply {
                                    addClassName("template-detail-list-label")
                                },
                            )
                            add(
                                Div().apply {
                                    addClassName("template-detail-custom-field-meta")
                                    add(
                                        Span(typeLabel).apply { addClassName("template-field-type") },
                                        Span(if (field.mandatory) "Mandatory" else "Optional").apply {
                                            addClassName("template-field-badge")
                                            element.setAttribute(
                                                "data-requirement",
                                                if (field.mandatory) "mandatory" else "optional",
                                            )
                                        },
                                    )
                                },
                            )
                            if (field.enumOptions.isNotEmpty()) {
                                add(
                                    Span(field.enumOptions.joinToString(", ")).apply {
                                        addClassName("template-detail-custom-field-options")
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }

    private fun keyValueRow(
        label: String,
        value: String,
        placeholder: Boolean = false,
    ): Div =
        Div().apply {
            addClassName("template-detail-kv")
            add(
                Span(label).apply { addClassName("template-detail-kv-label") },
                Span(value).apply {
                    addClassName("template-detail-kv-value")
                    if (placeholder) addClassName("template-field-placeholder")
                },
            )
        }

    private data class StandardFieldDisplay(
        val label: String,
        val value: String,
        val placeholder: Boolean,
    )

    private fun emptySectionMessage(message: String): Span =
        Span(message).apply {
            addClassName("template-detail-section-empty")
        }
}
