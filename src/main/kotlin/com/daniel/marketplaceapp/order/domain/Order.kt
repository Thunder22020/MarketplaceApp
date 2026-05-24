package com.daniel.marketplaceapp.order.domain

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.exception.EmptyOrderException
import com.daniel.marketplaceapp.order.exception.OrderItemNotFoundException
import com.daniel.marketplaceapp.product.domain.Product
import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.exception.ProductNotFoundException
import java.time.Instant
import java.util.UUID


class Order(
    var id: UUID?,
    var status: OrderStatus,
    var totalAmount: Money,
    val customerId: UUID,
    val createdAt: Instant,
    var updatedAt: Instant?,
    val items: MutableList<OrderItem>,
    var version: Long? = null,
) {
    fun refreshItemsBeforeCheckout(
        orderItemsByProductIds: Map<UUID, OrderItem>,
        productsByIds: Map<UUID, Product>
    ): Boolean {
        var anyPriceChanged = false
        var anyStatusChanged = false
        var totalAmount = Money.ZERO
        productsByIds.forEach { (productId, product) ->
            val orderItem = requireNotNull(orderItemsByProductIds[productId])
            if (product.status != ProductStatus.ACTIVE) {
                anyStatusChanged = true
                this.items.remove(orderItem)
                return@forEach
            }
            if (product.price != orderItem.unitPrice) {
                anyPriceChanged = true
            }
            orderItem.unitPrice = product.price
            totalAmount += product.price * orderItem.quantity
        }

        val missingProductIds = orderItemsByProductIds.keys - productsByIds.keys
        if (missingProductIds.isNotEmpty()) {
            anyStatusChanged = true
            this.items.removeIf { it.productId in missingProductIds }
        }

        this.totalAmount = totalAmount

        val changed = anyStatusChanged || anyPriceChanged
        if (changed) {
            setUpdatedAt()
        }
        return changed
    }

    fun markPaid() {
        this.status = OrderStatus.PAID
        setUpdatedAt()
    }

    fun markFailed() {
        this.status = OrderStatus.FAILED
        setUpdatedAt()
    }

    fun updateStatus(newStatus: OrderStatus) {
        this.status = newStatus
        setUpdatedAt()
    }

    fun addItemToCart(product: Product) {
        if (product.status != ProductStatus.ACTIVE) {
            throw ProductNotFoundException("Product with id ${product.id} not found")
        }
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
                sellerId = product.sellerId
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

    fun checkItemsNotEmptyOrThrow() {
        if (this.items.isEmpty()) {
            throw EmptyOrderException("No items found for customer $customerId")
        }
    }
}
