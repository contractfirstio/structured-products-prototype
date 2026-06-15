package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.repository.LegSchemaRepository
import com.contactfirstio.structuredproducts.data.repository.OrderRepository
import com.contactfirstio.structuredproducts.data.repository.ProductRepository
import com.contactfirstio.structuredproducts.data.repository.ProductTypeRepository
import org.springframework.stereotype.Service

@Service
class BlotterService(
    private val legSchemaRepository: LegSchemaRepository,
    private val productTypeRepository: ProductTypeRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val referentialIntegrityGuard: ReferentialIntegrityGuard,
    private val productTypeService: ProductTypeService,
    private val legSchemaService: LegSchemaService,
    private val productInstanceService: ProductInstanceService,
    private val orderService: OrderService,
) {

    fun loadSnapshot(): BlotterSnapshot {
        val typeNames = productTypeRepository.findAll().associate { it.id!! to it.name }
        val legSchemaNames = legSchemaService.findAll().associate { it.id to it.name }

        return BlotterSnapshot(
            legSchemas = legSchemaRepository.findAll()
                .map { leg ->
                    val legId = leg.id!!
                    val referenced = productTypeRepository.existsByAllowedLegSchemaIdsContaining(legId)
                    LegSchemaBlotterRow(
                        id = legId,
                        name = leg.name,
                        fieldsSummary = formatFieldDefinitions(leg.fields),
                        canEdit = !referenced,
                        canDelete = !referenced,
                    )
                }
                .sortedBy { it.name },
            productTypes = productTypeRepository.findAll()
                .map { type ->
                    val typeId = type.id!!
                    val referenced = productRepository.existsByTypeId(typeId)
                    ProductTypeBlotterRow(
                        id = typeId,
                        name = type.name,
                        requiresUnderlying = yesNo(type.globalTermsSchema.requiresUnderlying),
                        requiresMaturity = yesNo(type.globalTermsSchema.requiresMaturityDate),
                        legsSummary = type.allowedLegSchemaIds.joinToString(", ") { id ->
                            legSchemaNames[id] ?: id
                        },
                        canEdit = !referenced,
                        canDelete = !referenced,
                    )
                }
                .sortedBy { it.name },
            productInstances = productRepository.findAll()
                .map { product ->
                    val productId = product.id!!
                    val referenced = orderRepository.existsByProductId(productId)
                    ProductInstanceBlotterRow(
                        id = productId,
                        typeName = typeNames[product.typeId] ?: product.typeId,
                        underlying = product.underlying.ifBlank { "—" },
                        maturityMonths = product.maturityMonths,
                        status = product.status.name,
                        legsSummary = formatLegs(product.legs),
                        canEdit = !referenced,
                        canDelete = !referenced,
                    )
                }
                .sortedByDescending { it.id },
            orders = orderRepository.findAll()
                .map { order ->
                    val product = productRepository.findById(order.productId).orElse(null)
                    OrderBlotterRow(
                        id = order.id!!,
                        productId = order.productId,
                        productUnderlying = product?.underlying?.ifBlank { "—" } ?: "—",
                        notionalInvested = order.notionalInvested,
                        status = order.status.name,
                        canEdit = true,
                        canDelete = true,
                    )
                }
                .sortedByDescending { it.id },
        )
    }

    fun deleteLegSchema(id: String) {
        if (!legSchemaRepository.existsById(id)) {
            throw ValidationException("Leg schema not found: $id")
        }
        referentialIntegrityGuard.ensureLegSchemaIsUnreferenced(id, "delete")
        legSchemaRepository.deleteById(id)
    }

    fun updateLegSchema(id: String, command: CreateLegSchemaCommand): LegSchemaDetail {
        referentialIntegrityGuard.ensureLegSchemaIsUnreferenced(id, "edit")
        return legSchemaService.update(id, command)
    }

    fun getLegSchemaForEdit(id: String): LegSchemaDetail =
        legSchemaService.findById(id) ?: throw ValidationException("Leg schema not found: $id")

    fun deleteProductType(id: String) {
        if (!productTypeRepository.existsById(id)) {
            throw ValidationException("Product type not found: $id")
        }
        referentialIntegrityGuard.ensureProductTypeIsUnreferenced(id, "delete")
        productTypeRepository.deleteById(id)
    }

    fun deleteProductInstance(id: String) {
        if (!productRepository.existsById(id)) {
            throw ValidationException("Product instance not found: $id")
        }
        referentialIntegrityGuard.ensureProductInstanceIsUnreferenced(id, "delete")
        productRepository.deleteById(id)
    }

    fun deleteOrder(id: String) {
        if (!orderRepository.existsById(id)) {
            throw ValidationException("Order not found: $id")
        }
        orderRepository.deleteById(id)
    }

    fun updateProductType(id: String, command: CreateProductTypeCommand): ProductTypeDetail {
        referentialIntegrityGuard.ensureProductTypeIsUnreferenced(id, "edit")
        return productTypeService.update(id, command)
    }

    fun getProductInstanceForEdit(id: String): ProductInstanceEditDetail =
        productInstanceService.getForEdit(id)

    fun updateProductInstance(id: String, command: CreateProductInstanceCommand): SavedProductResult {
        referentialIntegrityGuard.ensureProductInstanceIsUnreferenced(id, "edit")
        return productInstanceService.updateProduct(id, command)
    }

    fun getOrderForEdit(id: String): OrderEditDetail = orderService.getForEdit(id)

    fun updateOrder(id: String, productId: String, notionalInvested: Double): SubmittedOrderResult =
        orderService.updateOrder(id, productId, notionalInvested)

    fun availableLegSchemas(): List<LegSchemaDetail> = legSchemaService.findAll()

    fun getProductTypeForEdit(id: String): ProductTypeDetail =
        productTypeService.findById(id) ?: throw ValidationException("Product type not found: $id")

    private fun yesNo(value: Boolean): String = if (value) "Yes" else "No"

    private fun formatFieldDefinitions(fields: List<com.contactfirstio.structuredproducts.data.document.FieldDefinition>): String =
        fields.joinToString(", ") { field ->
            val required = if (field.isRequired) "required" else "optional"
            "${field.fieldName} (${field.dataType.name}, $required)"
        }

    private fun formatLegs(legs: List<Map<String, Any>>): String =
        legs.joinToString(" · ") { leg ->
            leg.filterKeys { it != "legSchemaId" }
                .entries
                .joinToString(", ") { (key, value) -> "$key: $value" }
        }
}
