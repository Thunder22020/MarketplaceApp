package com.daniel.marketplaceapp.product.repository

import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductRepository : JpaRepository<ProductEntity, UUID> {
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id AND p.seller.id = :sellerId")
    fun findByIdAndSellerId(id: UUID, sellerId: UUID): ProductEntity?

    fun findByIdAndStatusIs(id: UUID, status: ProductStatus): ProductEntity?

    fun findAllByStatusOrderByCreatedAtDesc(status: ProductStatus): List<ProductEntity>

    fun findAllBySellerIdAndStatusInOrderByCreatedAtDesc(
        sellerId: UUID,
        statusList: Collection<ProductStatus>,
    ): List<ProductEntity>
}
