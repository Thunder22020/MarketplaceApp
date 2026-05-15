package com.daniel.marketplaceapp.order.entity

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.exception.OrderItemNotFoundException
import com.daniel.marketplaceapp.product.entity.Product
import com.daniel.marketplaceapp.user.entity.User
import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    )
    var totalAmount: Money,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    var customer: User,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = true)
    var updatedAt: Instant? = null,

    @OneToMany(
        mappedBy = "order",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var items: MutableList<OrderItem> = mutableListOf(),

    @Version
    @Column(nullable = false)
    var version: Long? = null
) {
    fun addItemToCart(product: Product) {
        val existingItem = this.items.firstOrNull { it.product.id == product.id }

        if (existingItem != null) {
            existingItem.quantity += 1
            this.totalAmount += existingItem.unitPrice
            setUpdatedAt()
        } else {
            val orderItem = OrderItem(
                product = product,
                unitPrice = product.price.copy(),
                quantity = 1
            )
            this.addItem(orderItem)
        }
    }

    fun deleteItemFromCart(productId: UUID) {
        val existingItem = this.items.firstOrNull { it.product.id == productId }
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
        item.order = this
        this.totalAmount += item.unitPrice
    }

    private fun removeItemFromCart(item: OrderItem) {
        this.items.remove(item)
        item.order = null
        this.totalAmount -= item.unitPrice
    }

    private fun setUpdatedAt() {
        this.updatedAt = Instant.now()
    }
}
