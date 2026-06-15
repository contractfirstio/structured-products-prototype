package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.OrderDocument
import com.contactfirstio.structuredproducts.data.document.OrderStatus
import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.contactfirstio.structuredproducts.data.repository.OrderRepository
import com.contactfirstio.structuredproducts.data.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val productQueryService: ProductQueryService,
) {

    fun submitOrder(productId: String, notionalInvested: Double): SubmittedOrderResult {
        validateOrderTarget(productId, notionalInvested)

        val saved = orderRepository.save(
            OrderDocument(
                productId = productId,
                notionalInvested = notionalInvested,
                status = OrderStatus.SUBMITTED,
            ),
        )

        return toSubmittedResult(saved)
    }

    fun getForEdit(id: String): OrderEditDetail {
        val order = orderRepository.findById(id)
            .orElseThrow { ValidationException("Order not found: $id") }

        return OrderEditDetail(
            id = order.id!!,
            productId = order.productId,
            notionalInvested = order.notionalInvested,
            activeProducts = productQueryService.findActiveProducts(),
        )
    }

    fun updateOrder(id: String, productId: String, notionalInvested: Double): SubmittedOrderResult {
        val existing = orderRepository.findById(id)
            .orElseThrow { ValidationException("Order not found: $id") }

        validateOrderTarget(productId, notionalInvested)

        val saved = orderRepository.save(
            existing.copy(
                productId = productId,
                notionalInvested = notionalInvested,
            ),
        )

        return toSubmittedResult(saved)
    }

    private fun validateOrderTarget(productId: String, notionalInvested: Double) {
        val product = productRepository.findById(productId)
            .orElseThrow { ValidationException("Product not found: $productId") }

        if (product.status != ProductStatus.ACTIVE) {
            throw ValidationException("Only ACTIVE products can be traded")
        }
        if (notionalInvested <= 0.0) {
            throw ValidationException("Notional amount must be positive")
        }
    }

    private fun toSubmittedResult(order: OrderDocument): SubmittedOrderResult {
        val product = productRepository.findById(order.productId).orElse(null)
        return SubmittedOrderResult(
            orderId = order.id!!,
            productUnderlying = product?.underlying?.ifBlank { "—" } ?: "—",
        )
    }
}

data class SubmittedOrderResult(
    val orderId: String,
    val productUnderlying: String,
)
