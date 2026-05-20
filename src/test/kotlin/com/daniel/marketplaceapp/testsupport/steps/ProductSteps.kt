package com.daniel.marketplaceapp.testsupport.steps

import com.daniel.marketplaceapp.product.domain.Product
import com.daniel.marketplaceapp.product.dto.CreateProductRequest
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.repository.ProductRepository
import com.daniel.marketplaceapp.product.service.ProductService
import com.daniel.marketplaceapp.testsupport.fixtures.randomString
import java.math.BigDecimal
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class ProductSteps(
    private val productService: ProductService,
    private val productRepository: ProductRepository
) {
    fun create(
        sellerId: UUID,
        title: String = randomString(),
        description: String? = randomString(),
        price: BigDecimal = BigDecimal("100.00"),
    ): Product {
        val req = CreateProductRequest(
            title = title,
            description = description,
            price = price
        )
        return productService.create(req, sellerId)
    }

    fun update(
        req: UpdateProductRequest,
        productId: UUID,
    ): Product {
        val product = productService.getByIdOrThrow(productId)
        product.update(req)
        return productRepository.save(product)
    }

    fun hide(productId: UUID, sellerId: UUID) {
        productService.hide(productId, sellerId)
    }
}
