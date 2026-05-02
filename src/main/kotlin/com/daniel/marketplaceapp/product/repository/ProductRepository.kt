package com.daniel.marketplaceapp.product.repository

import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductRepository : JpaRepository<Product, UUID> {
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.seller.id = :sellerId")
    fun findByIdAndSellerId(id: UUID, sellerId: UUID): Product?

    fun findAllByStatusOrderByCreatedAtDesc(status: ProductStatus): List<Product>

    fun findAllBySellerIdAndStatusInOrderByCreatedAtDesc(
        sellerId: UUID,
        statusList: Collection<ProductStatus>,
    ): List<Product>
}
