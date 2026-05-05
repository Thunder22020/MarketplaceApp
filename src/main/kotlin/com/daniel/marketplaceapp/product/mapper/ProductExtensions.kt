package com.daniel.marketplaceapp.product.mapper

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.product.dto.ProductResponse
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.model.Product
import java.time.Instant


fun Product.updateFrom(req: UpdateProductRequest) {
    req.title?.let { title = it }
    req.description?.let { description = it }
    req.price?.let { price = Money(it) }
    updatedAt = Instant.now()
}

fun Product.toResponse() = ProductResponse(
    id = requireNotNull(id),
    title = title,
    description = description,
    price = price.amount,
    sellerId = requireNotNull(seller.id),
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)
