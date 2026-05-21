package com.daniel.marketplaceapp.order.mapper

import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.dto.CheckoutResponse
import com.daniel.marketplaceapp.order.dto.CheckoutResult
import com.daniel.marketplaceapp.order.dto.OrderItemResponse
import com.daniel.marketplaceapp.order.dto.OrderResponse
import com.daniel.marketplaceapp.order.dto.PaymentCheckoutResponse

fun OrderItem.toResponse() = OrderItemResponse(
    orderId = requireNotNull(orderId),
    productId = requireNotNull(productId),
    unitPrice = unitPrice.amount,
    quantity = quantity,
)

fun Order.toResponse() = OrderResponse(
    id = requireNotNull(id),
    status = status,
    totalAmount = totalAmount,
    customerId = customerId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    items = items.map { it.toResponse() }.toMutableList(),
)

fun CheckoutResult.toResponse() = CheckoutResponse(
    order = order.toResponse(),
    payment = PaymentCheckoutResponse(
        externalPaymentId = requireNotNull(payment.externalId),
        confirmationUrl = requireNotNull(payment.confirmationUrl)
    )
)
