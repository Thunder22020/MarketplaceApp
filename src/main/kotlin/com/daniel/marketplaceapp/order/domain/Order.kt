package com.daniel.marketplaceapp.order.domain

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.exception.OrderItemNotFoundException
import com.daniel.marketplaceapp.product.domain.Product
import java.time.Instant
import java.util.UUID


class Order(
    var id: UUID?,
    val status: OrderStatus,
    var totalAmount: Money,
    val customerId: UUID,
    val createdAt: Instant,
    var updatedAt: Instant?,
    val items: MutableList<OrderItem>,
    var version: Long? = null,
) {
    fun addItemToCart(product: Product) {
        val existingItem = this.items.firstOrNull { it.productId == product.id }

        if (existingItem != null) {
            existingItem.quantity += 1
            this.totalAmount += existingItem.unitPrice
            setUpdatedAt()
        } else {
            val orderItem = OrderItem(
                productId = requireNotNull(product.id),
                unitPrice = product.price.copy(),
                quantity = 1,
                orderId = this.id,
                id = null
            )
            this.addItem(orderItem)
        }
    }

    fun deleteItemFromCart(productId: UUID) {
        val existingItem = this.items.firstOrNull { it.productId == productId }
            ?: throw OrderItemNotFoundException("Order item for product $productId not found")

        if (existingItem.quantity > 1) {
            existingItem.quantity -= 1
            this.totalAmount -= existingItem.unitPrice
        } else if (existingItem.quantity == 1) {
            removeItemFromCart(existingItem)
        }
        setUpdatedAt()
    }

    private fun addItem(item: OrderItem) {
        this.items.add(item)
        item.orderId = this.id
        this.totalAmount += item.unitPrice
    }

    private fun removeItemFromCart(item: OrderItem) {
        this.items.remove(item)
        item.orderId = this.id
        this.totalAmount -= item.unitPrice
    }

    private fun setUpdatedAt() {
        this.updatedAt = Instant.now()
    }
}
