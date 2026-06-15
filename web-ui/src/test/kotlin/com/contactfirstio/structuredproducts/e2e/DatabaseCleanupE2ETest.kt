package com.contactfirstio.structuredproducts.e2e

import com.contactfirstio.structuredproducts.data.repository.OrderRepository
import com.contactfirstio.structuredproducts.data.repository.ProductRepository
import com.contactfirstio.structuredproducts.data.repository.ProductTypeRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

abstract class DatabaseCleanupE2ETest : BaseE2ETest() {

    @Autowired
    protected lateinit var orderRepository: OrderRepository

    @Autowired
    protected lateinit var productRepository: ProductRepository

    @Autowired
    protected lateinit var productTypeRepository: ProductTypeRepository

    @BeforeEach
    fun cleanDatabase() {
        orderRepository.deleteAll()
        productRepository.deleteAll()
        productTypeRepository.deleteAll()
    }
}
