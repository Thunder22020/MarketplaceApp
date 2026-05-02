package com.daniel.marketplaceapp.product.controller

import com.daniel.marketplaceapp.core.annotations.CurrentUserId
import com.daniel.marketplaceapp.product.dto.CreateProductRequest
import com.daniel.marketplaceapp.product.dto.ProductResponse
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.mapper.toResponse
import com.daniel.marketplaceapp.product.service.ProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/products")
class ProductController(
    val productService: ProductService
) {
    @PostMapping
    fun create(
        @Valid @RequestBody req: CreateProductRequest,
        @CurrentUserId currentUserId: UUID
    ): ResponseEntity<ProductResponse> {
        val product = productService.create(req, currentUserId).toResponse()
        return ResponseEntity.status(HttpStatus.CREATED).body(product)
    }

    @PutMapping("/{productId}")
    fun update(
        @Valid @RequestBody req: UpdateProductRequest,
        @PathVariable productId: UUID,
        @CurrentUserId currentUserId: UUID
    ) = productService.update(req, productId, currentUserId).toResponse()

    @DeleteMapping("/{productId}")
    fun delete(
        @PathVariable productId: UUID,
        @CurrentUserId currentUserId: UUID
    ): ResponseEntity<Void> {
        productService.delete(productId, currentUserId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping(params = ["sellerId"])
    fun getAllBySellerId(
        @RequestParam("sellerId", required = true) sellerId: UUID,
        @CurrentUserId currentUserId: UUID
    ) = productService.findAllBySellerId(sellerId, currentUserId).map { it.toResponse() }

    @GetMapping
    fun getAll() = productService.findAll().map { it.toResponse() }
}
