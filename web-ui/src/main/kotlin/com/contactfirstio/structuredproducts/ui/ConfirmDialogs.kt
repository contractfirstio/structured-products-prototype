package com.contactfirstio.structuredproducts.ui

import com.vaadin.flow.component.confirmdialog.ConfirmDialog

object ConfirmDialogs {
    fun confirmDelete(
        itemLabel: String,
        onConfirm: () -> Unit,
    ) {
        ConfirmDialog().apply {
            setHeader("Delete template?")
            setText("Delete \"$itemLabel\"? This cannot be undone.")
            setCancelable(true)
            setConfirmText("Delete")
            setConfirmButtonTheme("error primary")
            addConfirmListener { onConfirm() }
        }.open()
    }
}
