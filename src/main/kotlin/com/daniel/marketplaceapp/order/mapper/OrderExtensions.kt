package com.daniel.marketplaceapp.order.mapper

import com.daniel.marketplaceapp.order.dto.OrderItemResponse
import com.daniel.marketplaceapp.order.entity.OrderItem

fun OrderItem.toResponse() = OrderItemResponse(
    id = requireNotNull(id),
    orderId = requireNotNull(order?.id),
    productId = requireNotNull(product.id),
    unitPrice = unitPrice.amount,
    quantity = quantity,
)
