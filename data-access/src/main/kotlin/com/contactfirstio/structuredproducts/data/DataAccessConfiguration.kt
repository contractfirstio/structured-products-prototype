package com.contactfirstio.structuredproducts.data

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(basePackages = ["com.contactfirstio.structuredproducts.data.repository"])
class DataAccessConfiguration
