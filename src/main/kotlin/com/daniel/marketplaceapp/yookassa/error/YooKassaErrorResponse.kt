package com.daniel.marketplaceapp.yookassa.error

data class YooKassaErrorResponse(
    val type: String,
    val id: String,
    val code: String,
    val description: String,
    val parameter: String? = null
)
