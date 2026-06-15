package com.contactfirstio.structuredproducts.service

class ValidationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
