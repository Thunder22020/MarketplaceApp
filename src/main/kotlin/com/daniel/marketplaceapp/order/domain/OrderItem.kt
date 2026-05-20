package com.daniel.marketplaceapp.order.domain

import com.daniel.marketplaceapp.core.domain.Money
import java.util.UUID

class OrderItem(
    var orderId: UUID?,
    var productId: UUID,
    var unitPrice: Money,
    var quantity: Int,
) {
    fun totalPrice() = unitPrice * quantity
}
