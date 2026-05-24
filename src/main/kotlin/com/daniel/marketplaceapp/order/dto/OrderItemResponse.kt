package com.daniel.marketplaceapp.order.dto

import java.math.BigDecimal
import java.util.UUID

data class OrderItemResponse(
    val orderId: UUID,
    val productId: UUID,
    val sellerId: UUID,
    var unitPrice: BigDecimal,
    var quantity: Int,
)
