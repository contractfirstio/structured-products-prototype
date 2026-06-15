package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.repository.OrderRepository
import com.contactfirstio.structuredproducts.data.repository.ProductRepository
import com.contactfirstio.structuredproducts.data.repository.ProductTypeRepository
import com.contactfirstio.structuredproducts.dsl.LegProcessorCatalog
import org.springframework.stereotype.Service

@Service
class BlotterService(
    private val productTypeRepository: ProductTypeRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val referentialIntegrityGuard: ReferentialIntegrityGuard,
    private val productTypeService: ProductTypeService,
    private val productInstanceService: ProductInstanceService,
    private val orderService: OrderService,
) {

    fun loadSnapshot(): BlotterSnapshot {
        val typeNames = productTypeRepository.findAll().associate { it.id!! to it.name }

        return BlotterSnapshot(
            productTypes = productTypeRepository.findAll()
                .map { type ->
                    val typeId = type.id!!
                    val referenced = productRepository.existsByTypeId(typeId)
                    ProductTypeBlotterRow(
                        id = typeId,
                        name = type.name,
                        requiresUnderlying = yesNo(type.globalTermsSchema.requiresUnderlying),
                        requiresMaturity = yesNo(type.globalTermsSchema.requiresMaturityDate),
                        legsSummary = type.legSchemas.joinToString(", ") { leg ->
                            val mandatory = if (leg.isRequired) "required" else "optional"
                            "${leg.legType} ($mandatory)"
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

    fun availableLegProcessorOptions() = productTypeService.availableLegProcessorOptions()

    fun getProductTypeForEdit(id: String): ProductTypeDetail =
        productTypeService.findById(id) ?: throw ValidationException("Product type not found: $id")

    private fun yesNo(value: Boolean): String = if (value) "Yes" else "No"

    private fun formatLegs(legs: List<Map<String, Any>>): String =
        legs.joinToString(" · ") { leg ->
            val type = leg["type"]?.toString() ?: "unknown"
            val definition = LegProcessorCatalog.findByProcessorLegType(type)
            val parameterKey = definition?.parameterKey
            val value = parameterKey?.let { leg[it] } ?: leg.filterKeys { it != "type" }.values.firstOrNull()
            val label = definition?.displayName ?: type
            "$label: $value"
        }
}
