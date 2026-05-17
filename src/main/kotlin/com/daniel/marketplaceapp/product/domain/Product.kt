package com.daniel.marketplaceapp.product.domain

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.exception.ProductAlreadyDeletedException
import java.time.Instant
import java.util.UUID

class Product(
    var id: UUID?,
    var title: String,
    var description: String?,
    var price: Money,
    var sellerId: UUID,
    var status: ProductStatus,
    var createdAt: Instant,
    var updatedAt: Instant?,
) {
    fun deleteProduct() {
        status = ProductStatus.DELETED
        setUpdatedAt()
    }

    fun updateStatus(newStatus: ProductStatus) {
        checkProductNotDeletedOrThrow()
        status = newStatus
        setUpdatedAt()
    }

    fun update(req: UpdateProductRequest) {
        checkProductNotDeletedOrThrow()
        req.title?.let { title = it }
        req.description?.let { description = it }
        req.price?.let { price = Money(it) }
        setUpdatedAt()
    }

    private fun checkProductNotDeletedOrThrow() {
        if (status == ProductStatus.DELETED) {
            throw ProductAlreadyDeletedException("Product $id has been deleted")
        }
    }

    private fun setUpdatedAt() {
        updatedAt = Instant.now()
    }
}
