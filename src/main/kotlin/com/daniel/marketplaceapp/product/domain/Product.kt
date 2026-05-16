package com.daniel.marketplaceapp.product.domain

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.product.enums.ProductStatus
import java.time.Instant
import java.util.UUID

class Product(
    var id: UUID?,
    val title: String,
    var description: String?,
    var price: Money,
    var sellerId: UUID,
    var status: ProductStatus,
    var createdAt: Instant,
    var updatedAt: Instant?,
)
