package com.daniel.marketplaceapp.order.service

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.dto.CheckoutResult
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.exception.OrderNotFoundException
import com.daniel.marketplaceapp.order.exception.SomeProductsHaveChangedException
import com.daniel.marketplaceapp.order.repository.OrderRepository
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import com.daniel.marketplaceapp.payment.exception.PaymentCreationFailedException
import com.daniel.marketplaceapp.payment.exception.PaymentNotFoundException
import com.daniel.marketplaceapp.payment.service.PaymentService
import com.daniel.marketplaceapp.product.service.ProductService
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productService: ProductService,
    private val paymentService: PaymentService,
) {
    @Transactional(
        noRollbackFor = [
            SomeProductsHaveChangedException::class,
            PaymentCreationFailedException::class
        ]
    )
    fun checkout(customerId: UUID): CheckoutResult {
        val order = getDraftOrderOrThrow(customerId)
        order.checkItemsNotEmptyOrThrow()
        val orderItemsByProductIds = order.items.associateBy { it.productId }
        val productsByIds = productService.getAllByIds(orderItemsByProductIds.keys).associateBy { it.id!! }
        val hasChanges = order.refreshItemsBeforeCheckout(orderItemsByProductIds, productsByIds)
        if (hasChanges) {
            orderRepository.save(order)
            throw SomeProductsHaveChangedException("Order ${order.id} has been changed")
        }
        order.updateStatus(OrderStatus.PENDING_PAYMENT)
        val savedOrder = orderRepository.save(order)

        val payment = try {
            paymentService.createPaymentForOrder(savedOrder)
        } catch (e: PaymentCreationFailedException) {
            savedOrder.updateStatus(OrderStatus.FAILED)
            orderRepository.save(savedOrder)
            throw e
        }

        return CheckoutResult(
            order = savedOrder,
            payment = payment
        )
    }

    @Transactional
    fun cancelOrder(customerId: UUID) {
        val order = getPendingOrderOrThrow(customerId)
        if (order.status != OrderStatus.PENDING_PAYMENT) {
            throw OrderNotFoundException("Order ${order.id} in PENDING_PAYMENT status not found")
        }
        val payment = paymentService.getByOrderId(order.id!!)
        if (payment.status != PaymentStatus.PENDING) {
            throw PaymentNotFoundException("Payment ${order.id} in PENDING status not found")
        }
        paymentService.cancelPayment(payment.externalId!!)
    }

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

    private fun getDraftOrderOrThrow(customerId: UUID) =
        getDraftOrder(customerId)
            ?: throw OrderNotFoundException("Order was not found for customer $customerId")

    private fun getPendingOrderOrThrow(customerId: UUID) =
        orderRepository.findPendingByCustomerId(customerId)
            ?: throw OrderNotFoundException("Order was not found for customer $customerId")
}
