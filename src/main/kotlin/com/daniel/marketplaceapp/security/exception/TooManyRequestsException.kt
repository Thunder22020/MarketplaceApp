package com.daniel.marketplaceapp.security.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class TooManyRequestsException(
    msg: String,
    val retryAfterValue: String
): BaseApiException(
    msg,
    HttpStatus.TOO_MANY_REQUESTS,
    "TOO_MANY_REQUESTS"
)
