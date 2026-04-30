package com.daniel.marketplaceapp.core.exception

import org.springframework.http.HttpStatus

abstract class BaseApiException(
    message: String,
    val status: HttpStatus,
    val code: String
) : RuntimeException(message)
