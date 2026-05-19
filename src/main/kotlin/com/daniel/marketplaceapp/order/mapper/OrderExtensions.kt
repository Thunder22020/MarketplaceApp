package com.daniel.marketplaceapp.order.mapper

import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.dto.OrderItemResponse

fun OrderItem.toResponse() = OrderItemResponse(
    id = requireNotNull(id),
    orderId = requireNotNull(orderId),
    productId = requireNotNull(productId),
    unitPrice = unitPrice.amount,
    quantity = quantity,
)
