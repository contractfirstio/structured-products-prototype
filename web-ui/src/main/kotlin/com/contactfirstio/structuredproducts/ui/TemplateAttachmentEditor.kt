package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.service.TemplateAttachmentService
import com.contactfirstio.structuredproducts.service.TemplateAttachmentSummary
import com.contactfirstio.structuredproducts.service.ValidationException
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.server.StreamResource
import java.io.ByteArrayInputStream

class TemplateAttachmentEditor(
    private val field: CatalogFieldDefinition,
    private val templateId: String?,
    private val attachmentService: TemplateAttachmentService,
    currentValue: String?,
    private val onValueChange: (String?) -> Unit,
) : HorizontalLayout() {

    private val attachmentIds = attachmentService.parseAttachmentIds(currentValue).toMutableList()
    private val summaryButton =
        Button().apply {
            addClassName("template-attachment-button")
            width = "100%"
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
            addClickListener { openDialog() }
        }

    init {
        addClassName("template-attachment-editor")
        isPadding = false
        setWidthFull()
        add(summaryButton)
        refreshSummaryButton()
    }

    private fun refreshSummaryButton() {
        val count = attachmentIds.size
        summaryButton.text =
            when (count) {
                0 -> "Add files"
                1 -> "1 file"
                else -> "$count files"
            }
        summaryButton.icon = if (count == 0) VaadinIcon.PLUS.create() else VaadinIcon.PAPERCLIP.create()
    }

    private fun publishValue() {
        onValueChange(attachmentService.serializeAttachmentIds(attachmentIds))
    }

    private fun openDialog() {
        val dialog =
            Dialog().apply {
                headerTitle = field.displayName
                isModal = true
                width = "28rem"
            }

        val fileList = VerticalLayout().apply {
            isPadding = false
            setWidthFull()
            addClassName("template-attachment-list")
        }

        fun refreshFileList() {
            fileList.removeAll()
            val summaries = attachmentService.findSummaries(attachmentIds)
            if (summaries.isEmpty()) {
                fileList.add(
                    Span("No files attached yet.").apply {
                        addClassName("template-field-placeholder")
                    },
                )
                return
            }

            summaries.forEach { summary ->
                fileList.add(
                    createFileRow(summary) {
                        attachmentIds.remove(summary.id)
                        attachmentService.delete(summary.id)
                        publishValue()
                        refreshSummaryButton()
                        refreshFileList()
                    },
                )
            }
        }

        val upload =
            Upload { event ->
                try {
                    val content = event.inputStream.use { it.readBytes() }
                    val saved =
                        attachmentService.store(
                            templateId = templateId,
                            fieldKey = field.key,
                            fileName = event.fileName,
                            contentType = event.contentType,
                            content = content,
                        )
                    attachmentIds.add(saved.id)
                    publishValue()
                    refreshSummaryButton()
                    dialog.ui.ifPresent { ui ->
                        ui.access {
                            refreshFileList()
                            Notification.show("Uploaded ${saved.fileName}", 3000, Notification.Position.BOTTOM_START)
                        }
                    }
                } catch (ex: ValidationException) {
                    dialog.ui.ifPresent { ui ->
                        ui.access {
                            Notification.show(ex.message ?: "Upload failed", 5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR)
                        }
                    }
                    throw ex
                }
            }.apply {
                maxFileSize = TemplateAttachmentService.MAX_FILE_SIZE_BYTES
                uploadButton = Button("Upload file", VaadinIcon.UPLOAD.create())
                dropLabel = Span("or drop files here")
                width = "100%"
            }

        refreshFileList()

        dialog.add(
            VerticalLayout(upload, fileList).apply {
                isPadding = false
                setWidthFull()
                setSpacing(true)
            },
        )
        dialog.open()
    }

    private fun createFileRow(
        summary: TemplateAttachmentSummary,
        onRemove: () -> Unit,
    ): HorizontalLayout =
        HorizontalLayout().apply {
            addClassName("template-attachment-row")
            setWidthFull()
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
            isPadding = false

            val content = attachmentService.findById(summary.id)?.content ?: ByteArray(0)
            val downloadAnchor =
                Anchor(
                    StreamResource(summary.fileName) { ByteArrayInputStream(content) },
                    "",
                ).apply {
                    element.setAttribute("download", true)
                    add(
                        Button(summary.fileName, VaadinIcon.DOWNLOAD_ALT.create()).apply {
                            addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE)
                        },
                    )
                }

            val removeButton =
                Button(VaadinIcon.CLOSE_SMALL.create()).apply {
                    addClickListener { onRemove() }
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR)
                    element.setAttribute("aria-label", "Remove ${summary.fileName}")
                }

            add(downloadAnchor)
            expand(downloadAnchor)
            add(removeButton)
        }
}
