package com.daniel.marketplaceapp.core.exception

import java.time.Instant

data class ApiError(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val code: String,
    val message: String,
    val path: String,
    val details: List<ApiErrorDetail> = emptyList()
)

data class ApiErrorDetail(
    val field: String,
    val message: String,
)
