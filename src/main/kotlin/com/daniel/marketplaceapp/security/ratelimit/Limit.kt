package com.daniel.marketplaceapp.security.ratelimit

data class Limit(
    val max: Int,
    val windowSeconds: Int
)
