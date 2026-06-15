package com.contactfirstio.structuredproducts.e2e

import com.contactfirstio.structuredproducts.data.document.ProductStatus
import com.contactfirstio.structuredproducts.data.repository.LegSchemaRepository
import com.contactfirstio.structuredproducts.data.repository.OrderRepository
import com.contactfirstio.structuredproducts.data.repository.ProductRepository
import com.contactfirstio.structuredproducts.data.repository.ProductTypeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

abstract class DatabaseCleanupE2ETest : BaseE2ETest() {

    @Autowired
    protected lateinit var orderRepository: OrderRepository

    @Autowired
    protected lateinit var productRepository: ProductRepository

    @Autowired
    protected lateinit var productTypeRepository: ProductTypeRepository

    @Autowired
    protected lateinit var legSchemaRepository: LegSchemaRepository

    @BeforeEach
    fun cleanDatabase() {
        orderRepository.deleteAll()
        productRepository.deleteAll()
        productTypeRepository.deleteAll()
        legSchemaRepository.deleteAll()
    }

    protected fun seedPpnProductType(typeName: String = PPN_TYPE_NAME) {
        withPage("/admin/leg-builder") { it.createProtectionLegSchema() }
        withPage("/admin/leg-builder") { it.createUpsideLegSchema() }
        withPage("/admin/create-type") { it.createPpnProductType(typeName) }
    }

    protected fun seedActiveProduct(
        underlying: String,
        maturity: String,
        protection: String,
        participation: String,
        typeName: String = PPN_TYPE_NAME,
    ) {
        withPage("/create-product") { page ->
            page.selectProductType(typeName)
            page.fillPpnProductForm(underlying, maturity, protection, participation)
            page.submitProductForm()
            page.waitForNotification("Product saved as ACTIVE")
        }
    }

    protected fun seedDraftProduct(
        underlying: String,
        maturity: String,
        protection: String,
        typeName: String = PPN_TYPE_NAME,
    ) {
        withPage("/create-product") { page ->
            page.selectProductType(typeName)
            page.fillPpnProductForm(underlying, maturity, protection)
            page.submitProductForm()
            page.waitForNotification("Product saved as DRAFT")
        }
    }

    protected fun seedFullHierarchy(typeName: String = PPN_TYPE_NAME) {
        seedPpnProductType(typeName)
        seedActiveProduct("MSFT", "24", "100", "110", typeName)

        withPage("/order-entry") { page ->
            page.submitOrder("MSFT", "50000")
            page.waitForNotification("Order submitted for MSFT")
        }

        assertEquals(1, productTypeRepository.count())
        assertEquals(1, productRepository.findByStatus(ProductStatus.ACTIVE).size)
        assertEquals(1, orderRepository.count())
    }
}
