package com.daniel.marketplaceapp.core.event.payment

import com.daniel.marketplaceapp.order.domain.OrderItem
import java.util.UUID

data class PaymentSucceededEvent(
    val orderId: UUID,
    val paymentId: UUID,
    val orderItems: List<OrderItem>
)
