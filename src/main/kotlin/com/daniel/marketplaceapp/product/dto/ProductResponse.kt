package com.daniel.marketplaceapp.product.dto

import com.daniel.marketplaceapp.product.enums.ProductStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ProductResponse(
    var id: UUID,
    var title: String,
    var description: String?,
    var price: BigDecimal,
    var sellerId: UUID,
    var status: ProductStatus,
    var createdAt: Instant,
    var updatedAt: Instant?,
)
