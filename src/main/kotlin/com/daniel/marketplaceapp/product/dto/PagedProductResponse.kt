package com.daniel.marketplaceapp.product.dto

data class PagedProductResponse(
    val page: Int,
    val itemsCount: Int,
    val products: List<ProductResponse>
)
