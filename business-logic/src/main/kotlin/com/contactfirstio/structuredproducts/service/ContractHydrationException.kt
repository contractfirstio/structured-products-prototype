package com.contactfirstio.structuredproducts.service

class ContractHydrationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
