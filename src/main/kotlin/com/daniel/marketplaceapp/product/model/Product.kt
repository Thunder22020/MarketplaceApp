package com.daniel.marketplaceapp.product.model

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.user.entity.User
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = true)
    var description: String? = null,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "price", nullable = false, precision = 19, scale = 2)
    )
    var price: Money,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", referencedColumnName = "id", nullable = false)
    var seller: User,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ProductStatus,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = true)
    var updatedAt: Instant? = null,
)
