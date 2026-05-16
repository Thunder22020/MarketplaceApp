package com.daniel.marketplaceapp.product.mapper

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.product.domain.Product
import com.daniel.marketplaceapp.product.dto.ProductResponse
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.entity.ProductEntity
import com.daniel.marketplaceapp.user.entity.User
import java.time.Instant


fun ProductEntity.updateFrom(req: UpdateProductRequest) {
    req.title?.let { title = it }
    req.description?.let { description = it }
    req.price?.let { price = Money(it) }
    updatedAt = Instant.now()
}

fun ProductEntity.toResponse() = ProductResponse(
    id = requireNotNull(id),
    title = title,
    description = description,
    price = price.amount,
    sellerId = requireNotNull(seller.id),
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ProductEntity.toDomain() = Product(
    id = id,
    title = title,
    description = description,
    price = price.copy(),
    sellerId = requireNotNull(seller.id),
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Product.toEntity(seller: User) = ProductEntity(
    id = id,
    title = title,
    description = description,
    price = price.copy(),
    seller = seller,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)
