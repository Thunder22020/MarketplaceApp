package com.daniel.marketplaceapp.yookassa.error

class YooKassaApiException(
    val statusCode: Int,
    val error: YooKassaErrorResponse?,
    val rawBody: String,
    cause: Throwable
) : RuntimeException(
    error?.let {
        "YooKassa API error: status=$statusCode, code=${it.code}, description=${it.description}, parameter=${it.parameter}"
    } ?: "YooKassa API error: status=$statusCode, body=$rawBody",
    cause
)
