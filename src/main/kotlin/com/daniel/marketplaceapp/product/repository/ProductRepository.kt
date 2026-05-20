package com.daniel.marketplaceapp.product.repository

import com.daniel.marketplaceapp.product.domain.Product
import com.daniel.marketplaceapp.product.enums.ProductStatus
import java.util.UUID

interface ProductRepository {
    fun save(product: Product): Product

    fun findById(id: UUID): Product?

    fun findAllByIds(ids: Collection<UUID>): List<Product>

    fun findByIdAndSellerId(id: UUID, sellerId: UUID): Product?

    fun findAllByStatus(status: ProductStatus): List<Product>

    fun findAllBySellerIdAndStatusList(
        sellerId: UUID,
        statusList: Collection<ProductStatus>,
    ): List<Product>
}
