package com.contactfirstio.structuredproducts.e2e.cucumber

import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@CucumberContextConfiguration
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CucumberSpringConfiguration {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val mongoDBContainer: MongoDBContainer = MongoDBContainer("mongo:7.0")
    }
}
