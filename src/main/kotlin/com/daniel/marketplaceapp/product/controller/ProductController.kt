package com.daniel.marketplaceapp.product.controller

import com.daniel.marketplaceapp.core.annotations.CurrentUserId
import com.daniel.marketplaceapp.product.dto.CreateProductRequest
import com.daniel.marketplaceapp.product.dto.PagedProductResponse
import com.daniel.marketplaceapp.product.dto.ProductResponse
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.mapper.toResponse
import com.daniel.marketplaceapp.product.service.ProductService
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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

    @PatchMapping("/{productId}/hide")
    fun hide(
        @PathVariable productId: UUID,
        @CurrentUserId currentUserId: UUID
    ): ResponseEntity<Void> {
        productService.hide(productId, currentUserId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{productId}/unhide")
    fun unhide(
        @PathVariable productId: UUID,
        @CurrentUserId currentUserId: UUID
    ): ResponseEntity<Void> {
        productService.unhide(productId, currentUserId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping(params = ["sellerId"])
    fun getAllBySellerId(
        @RequestParam("sellerId", required = true) sellerId: UUID,
        @RequestParam("page", required = false) page: Int = 0,
        @CurrentUserId currentUserId: UUID
    ): PagedProductResponse {
        val products = productService.getAllBySellerId(
            sellerId,
            currentUserId,
            page
        ).map { it.toResponse() }
        return PagedProductResponse(
            page = page,
            itemsCount = products.size,
            products = products
        )
    }

    @GetMapping
    fun getAll(
        @RequestParam("page", required = false) page: Int = 0
    ): PagedProductResponse {
        val products = productService.getAll(page).map { it.toResponse() }
        return PagedProductResponse(
            page = page,
            itemsCount = products.size,
            products = products
        )
    }

}
