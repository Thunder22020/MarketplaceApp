package com.daniel.marketplaceapp.testsupport.steps

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.repository.OrderRepository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class OrderSteps(
    private val orderRepository: OrderRepository,
) {
    fun createOrder(
        customerId: UUID,
        status: OrderStatus = OrderStatus.DRAFT,
        items: MutableList<OrderItem> = mutableListOf(),
        totalAmount: Money = Money(BigDecimal("100.00")),
    ) = orderRepository.save(
        Order(
            id = null,
            status = status,
            totalAmount = totalAmount,
            customerId = customerId,
            createdAt = Instant.now(),
            updatedAt = null,
            items = items,
            version = null
        )
    )
}
