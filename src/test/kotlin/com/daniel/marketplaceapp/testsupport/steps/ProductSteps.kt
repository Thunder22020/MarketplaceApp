package com.daniel.marketplaceapp.testsupport.steps

import com.daniel.marketplaceapp.product.dto.CreateProductRequest
import com.daniel.marketplaceapp.product.entity.ProductEntity
import com.daniel.marketplaceapp.product.service.ProductService
import com.daniel.marketplaceapp.testsupport.fixtures.randomString
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class ProductSteps(
    private val productService: ProductService
) {
    fun createProduct(
        sellerId: UUID,
        title: String = randomString(),
        description: String? = randomString(),
        price: BigDecimal = BigDecimal("100.00"),
    ): ProductEntity {
        val req = CreateProductRequest(
            title = title,
            description = description,
            price = price
        )
        return productService.create(req, sellerId)
    }

    fun hideProduct(productId: UUID, sellerId: UUID) {
        productService.hide(productId, sellerId)
    }
}
