package com.daniel.marketplaceapp.order.controller

import com.daniel.marketplaceapp.core.annotations.CurrentUserId
import com.daniel.marketplaceapp.order.dto.OrderItemResponse
import com.daniel.marketplaceapp.order.mapper.OrderMapper
import com.daniel.marketplaceapp.order.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {
    @GetMapping
    fun getCartItems(@CurrentUserId customerId: UUID): List<OrderItemResponse> {
        return orderService.getCartItems(customerId).map { OrderMapper.toResponse(it) }
    }

    @PostMapping("/{productId}")
    fun addItemToCart(
        @PathVariable productId: UUID,
        @CurrentUserId currentUserId: UUID
    ): ResponseEntity<Void> {
        orderService.addItemToCart(productId, currentUserId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{productId}")
    fun removeItemFromCart(
        @PathVariable productId: UUID,
        @CurrentUserId currentUserId: UUID
    ): ResponseEntity<Void> {
        orderService.deleteItemFromCart(productId, currentUserId)
        return ResponseEntity.noContent().build()
    }
}
