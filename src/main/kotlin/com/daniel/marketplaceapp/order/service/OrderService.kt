package com.daniel.marketplaceapp.order.service

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.entity.Order
import com.daniel.marketplaceapp.order.entity.OrderItem
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.exception.OrderNotFoundException
import com.daniel.marketplaceapp.order.repository.OrderRepository
import com.daniel.marketplaceapp.product.service.ProductService
import com.daniel.marketplaceapp.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productService: ProductService,
    private val userService: UserService,
) {
    @Transactional
    fun addItemToCart(productId: UUID, customerId: UUID): Order {
        val order = getOrCreateDraftOrder(customerId)
        val product = productService.getByIdOrThrow(productId)
        order.addItemToCart(product)
        return order
    }

    @Transactional
    fun deleteItemFromCart(productId: UUID, customerId: UUID) {
        val order = getDraftOrderOrThrow(customerId)
        order.deleteItemFromCart(productId)
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
        return orderRepository.findDraftByCustomerIdWithItems(customerId)
    }

    private fun createDraftOrder(customerId: UUID): Order {
        val customer = userService.getByIdOrThrow(customerId)
        val order = Order(
            status = OrderStatus.DRAFT,
            totalAmount = Money(BigDecimal.ZERO),
            customer = customer,
        )
        return orderRepository.save(order)
    }

    private fun getDraftOrderOrThrow(customerId: UUID): Order {
        return getDraftOrder(customerId)
            ?: throw OrderNotFoundException("Order was not found for customer $customerId")
    }
}
