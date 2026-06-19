package com.daniel.marketplaceapp.core.event.payment

import com.daniel.marketplaceapp.core.domain.Money
import java.util.UUID

data class PaymentSucceededEvent(
    val orderId: UUID,
    val paymentId: UUID,
    val orderItems: List<OrderItemPayload>
)

data class OrderItemPayload(
    val sellerId: UUID,
    val unitPrice: Money,
    val quantity: Int
)
