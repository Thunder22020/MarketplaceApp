package com.daniel.marketplaceapp.order.mapper

import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.dto.OrderItemResponse
import com.daniel.marketplaceapp.order.dto.OrderResponse

fun OrderItem.toResponse() = OrderItemResponse(
    id = requireNotNull(id),
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
