package com.daniel.marketplaceapp.product.mapper

import com.daniel.marketplaceapp.product.domain.Product
import com.daniel.marketplaceapp.product.dto.ProductResponse

fun Product.toResponse() = ProductResponse(
    id = requireNotNull(id),
    title = title,
    description = description,
    price = price.amount,
    sellerId = sellerId,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)
