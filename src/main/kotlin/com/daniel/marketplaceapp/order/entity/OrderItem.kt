package com.daniel.marketplaceapp.order.entity

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.product.entity.Product
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    var order: Order? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    var product: Product,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    )
    var unitPrice: Money,

    @Column(nullable = false)
    var quantity: Int,
) {
    fun totalPrice() = this.unitPrice * this.quantity
}
