package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.service.CreateProductInstanceCommand
import com.contactfirstio.structuredproducts.service.FieldDefinitionDto
import com.contactfirstio.structuredproducts.service.LegSchemaDetail
import com.contactfirstio.structuredproducts.service.ProductTypeDetail
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.theme.lumo.LumoUtility

class DynamicProductFormBuilder {

    private var underlyingField: TextField? = null
    private var maturityField: IntegerField? = null
    private val legFieldGroups = mutableListOf<LegFieldGroup>()

    private data class LegFieldGroup(
        val legSchema: LegSchemaDetail,
        val fields: Map<String, Component>,
    )

    fun build(
        formLayout: FormLayout,
        productType: ProductTypeDetail,
        initialUnderlying: String? = null,
        initialMaturityMonths: Int? = null,
        initialLegData: List<Map<String, Any>> = emptyList(),
    ) {
        formLayout.removeAll()
        underlyingField = null
        maturityField = null
        legFieldGroups.clear()

        if (productType.globalTermsSchema.requiresUnderlying) {
            underlyingField = TextField("Underlying").apply {
                isRequired = true
                placeholder = "e.g. AAPL"
                value = initialUnderlying.orEmpty()
            }
            formLayout.add(underlyingField)
        }

        if (productType.globalTermsSchema.requiresMaturityDate) {
            maturityField = IntegerField("Maturity (months)").apply {
                isRequired = true
                min = 1
                step = 1
                value = initialMaturityMonths ?: 12
            }
            formLayout.add(maturityField)
        }

        productType.legSchemas.forEachIndexed { index, legSchema ->
            val initialValues = initialLegData.getOrNull(index).orEmpty()
            formLayout.add(
                H4(legSchema.name).apply {
                    addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.SMALL)
                },
            )

            val fields = linkedMapOf<String, Component>()
            legSchema.fields.forEach { definition ->
                val component = createFieldComponent(definition, initialValues[definition.fieldName])
                fields[definition.fieldName] = component
                formLayout.add(component)
            }

            legFieldGroups += LegFieldGroup(legSchema, fields)
        }
    }

    fun build(formLayout: FormLayout, productType: ProductTypeDetail) {
        build(formLayout, productType, null, null, emptyList())
    }

    private fun createFieldComponent(
        definition: FieldDefinitionDto,
        initialValue: Any?,
    ): Component =
        when (definition.dataType) {
            FieldDataType.STRING -> TextField(definition.fieldName).apply {
                isRequired = definition.isRequired
                value = initialValue?.toString().orEmpty()
            }

            FieldDataType.DOUBLE -> NumberField(definition.fieldName).apply {
                isRequired = definition.isRequired
                step = 0.01
                initialValue?.let { value = (it as? Number)?.toDouble() ?: it.toString().toDouble() }
                if (!definition.isRequired) {
                    helperText = "Leave blank to save as DRAFT (RFQ)."
                }
            }

            FieldDataType.INTEGER -> IntegerField(definition.fieldName).apply {
                isRequired = definition.isRequired
                step = 1
                initialValue?.let { value = (it as? Number)?.toInt() ?: it.toString().toInt() }
                if (!definition.isRequired) {
                    helperText = "Leave blank to save as DRAFT (RFQ)."
                }
            }

            FieldDataType.BOOLEAN -> Checkbox(definition.fieldName).apply {
                value = when (initialValue) {
                    is Boolean -> initialValue
                    is String -> initialValue.toBooleanStrictOrNull() ?: false
                    else -> false
                }
            }

            FieldDataType.ENUM -> ComboBox<String>(definition.fieldName).apply {
                isRequired = definition.isRequired
                setItems(definition.enumOptions)
                initialValue?.toString()?.let { value = it }
                if (!definition.isRequired) {
                    helperText = "Leave blank to save as DRAFT (RFQ)."
                }
            }
        }

    fun validate(): Boolean {
        var valid = true

        underlyingField?.let { field ->
            field.isInvalid = field.isEmpty
            if (field.isInvalid) valid = false
        }

        maturityField?.let { field ->
            field.isInvalid = field.isEmpty
            if (field.isInvalid) valid = false
        }

        legFieldGroups.forEach { group ->
            group.legSchema.fields.forEach { definition ->
                if (!definition.isRequired) return@forEach
                val component = group.fields[definition.fieldName] ?: return@forEach
                when (component) {
                    is TextField -> {
                        component.isInvalid = component.isEmpty
                        if (component.isInvalid) valid = false
                    }
                    is NumberField -> {
                        component.isInvalid = component.isEmpty
                        if (component.isInvalid) valid = false
                    }
                    is IntegerField -> {
                        component.isInvalid = component.isEmpty
                        if (component.isInvalid) valid = false
                    }
                    is ComboBox<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val combo = component as ComboBox<String>
                        combo.isInvalid = combo.isEmpty
                        if (combo.isInvalid) valid = false
                    }
                }
            }
        }

        return valid
    }

    fun toCommand(typeId: String): CreateProductInstanceCommand {
        val legData = legFieldGroups.map { group ->
            val data = linkedMapOf<String, Any>()
            group.legSchema.fields.forEach { definition ->
                val component = group.fields[definition.fieldName] ?: return@forEach
                extractValue(component, definition)?.let { data[definition.fieldName] = it }
            }
            data
        }

        return CreateProductInstanceCommand(
            typeId = typeId,
            underlying = underlyingField?.value?.trim(),
            maturityMonths = maturityField?.value,
            legData = legData,
        )
    }

    private fun extractValue(component: Component, definition: FieldDefinitionDto): Any? =
        when (component) {
            is TextField -> component.value.takeIf { it.isNotBlank() }
            is NumberField -> component.value.takeIf { !component.isEmpty }
            is IntegerField -> component.value.takeIf { !component.isEmpty }
            is Checkbox -> component.value
            is ComboBox<*> -> {
                @Suppress("UNCHECKED_CAST")
                (component as ComboBox<String>).value?.takeIf { it.isNotBlank() }
            }
            else -> null
        }

    fun clear() {
        underlyingField?.clear()
        maturityField?.value = 12
        legFieldGroups.forEach { group ->
            group.fields.values.forEach { component ->
                when (component) {
                    is TextField -> component.clear()
                    is NumberField -> component.clear()
                    is IntegerField -> component.clear()
                    is Checkbox -> component.value = false
                    is ComboBox<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        (component as ComboBox<String>).clear()
                    }
                }
            }
        }
    }
}
