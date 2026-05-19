package com.daniel.marketplaceapp.order.service

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.exception.OrderNotFoundException
import com.daniel.marketplaceapp.order.repository.OrderRepository
import com.daniel.marketplaceapp.product.service.ProductService
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productService: ProductService,
) {
    @Transactional
    fun addItemToCart(productId: UUID, customerId: UUID): Order {
        val order = getOrCreateDraftOrder(customerId)
        val product = productService.getByIdOrThrow(productId)
        order.addItemToCart(product)
        return orderRepository.save(order)
    }

    @Transactional
    fun deleteItemFromCart(productId: UUID, customerId: UUID) {
        val order = getDraftOrderOrThrow(customerId)
        order.deleteItemFromCart(productId)
        orderRepository.save(order)
    }

    @Transactional(readOnly = true)
    fun getCartItems(customerId: UUID): List<OrderItem> {
        val order = getDraftOrderOrThrow(customerId)
        return order.items
    }

    private fun getOrCreateDraftOrder(customerId: UUID): Order {
        return getDraftOrder(customerId) ?: createDraftOrder(customerId)
    }

    private fun getDraftOrder(customerId: UUID): Order? {
        return orderRepository.findDraftByCustomerId(customerId)
    }

    private fun createDraftOrder(customerId: UUID): Order {
        return Order(
            status = OrderStatus.DRAFT,
            totalAmount = Money.ZERO,
            customerId = customerId,
            id = null,
            createdAt = Instant.now(),
            updatedAt = null,
            items = mutableListOf(),
            version = null,
        )
    }

    private fun getDraftOrderOrThrow(customerId: UUID): Order {
        return getDraftOrder(customerId)
            ?: throw OrderNotFoundException("Order was not found for customer $customerId")
    }
}
