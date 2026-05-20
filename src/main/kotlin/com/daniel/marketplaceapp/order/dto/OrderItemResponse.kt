package com.daniel.marketplaceapp.order.dto

import java.math.BigDecimal
import java.util.UUID

data class OrderItemResponse(
    var orderId: UUID,
    var productId: UUID,
    var unitPrice: BigDecimal,
    var quantity: Int,
)
