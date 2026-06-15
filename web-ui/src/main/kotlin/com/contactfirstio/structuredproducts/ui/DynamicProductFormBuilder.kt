package com.contactfirstio.structuredproducts.ui

import com.contactfirstio.structuredproducts.service.CreateProductInstanceCommand
import com.contactfirstio.structuredproducts.service.ProductTypeDetail
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextField

class DynamicProductFormBuilder {

    private var underlyingField: TextField? = null
    private var maturityField: IntegerField? = null
    private val legFields = linkedMapOf<String, NumberField>()

    fun build(
        formLayout: FormLayout,
        productType: ProductTypeDetail,
        initialUnderlying: String? = null,
        initialMaturityMonths: Int? = null,
        initialLegValues: Map<String, Double?> = emptyMap(),
    ) {
        formLayout.removeAll()
        underlyingField = null
        maturityField = null
        legFields.clear()

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

        productType.legSchemas.forEach { legSchema ->
            val field = NumberField(legSchema.parameterLabel).apply {
                isRequired = legSchema.isRequired
                min = 0.0
                step = 1.0
                if (legSchema.processorLegType == "protection") {
                    max = 100.0
                }
                initialLegValues[legSchema.processorLegType]?.let { value = it }
                if (!legSchema.isRequired) {
                    helperText = "Leave blank to save as DRAFT (RFQ)."
                }
            }
            legFields[legSchema.processorLegType] = field
            formLayout.add(field)
        }
    }

    fun build(formLayout: FormLayout, productType: ProductTypeDetail) {
        build(formLayout, productType, null, null, emptyMap())
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

        legFields.forEach { (_, field) ->
            if (field.isRequired && field.isEmpty) {
                field.isInvalid = true
                valid = false
            } else {
                field.isInvalid = false
            }
        }

        return valid
    }

    fun toCommand(typeId: String): CreateProductInstanceCommand {
        val legValues = legFields.mapValues { (_, field) ->
            field.value.takeIf { !field.isEmpty }
        }

        return CreateProductInstanceCommand(
            typeId = typeId,
            underlying = underlyingField?.value?.trim(),
            maturityMonths = maturityField?.value,
            legValues = legValues,
        )
    }

    fun clear() {
        underlyingField?.clear()
        maturityField?.value = 12
        legFields.values.forEach { field ->
            if (field.label.orEmpty().contains("Protection")) {
                field.value = 100.0
            } else {
                field.clear()
            }
        }
    }
}
