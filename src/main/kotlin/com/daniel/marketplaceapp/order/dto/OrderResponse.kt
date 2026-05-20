package com.daniel.marketplaceapp.order.dto

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.enums.OrderStatus
import java.time.Instant
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val status: OrderStatus,
    val totalAmount: Money,
    val customerId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val items: MutableList<OrderItemResponse>,
)
