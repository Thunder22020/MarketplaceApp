package com.daniel.marketplaceapp.order.repository

import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.mapper.OrderMapper
import com.daniel.marketplaceapp.product.repository.ProductRepository
import com.daniel.marketplaceapp.user.exception.UserNotFoundException
import com.daniel.marketplaceapp.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaOrderRepository(
    private val springDataOrderRepository: SpringDataOrderRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) : OrderRepository {
    override fun findDraftByCustomerId(customerId: UUID): Order? {
        val order = springDataOrderRepository.findDraftByCustomerIdWithItems(customerId)
            ?: return null
        return OrderMapper.toDomain(order)
    }

    override fun save(order: Order): Order {
        val customer = findUserOrThrow(order.customerId)

        val productIds = order.items.map { it.productId }.toSet()
        val productsByIds = productRepository.findAllById(productIds)
            .associateBy { requireNotNull(it.id) }

        val entity = OrderMapper.toEntity(order, customer, productsByIds)
        return OrderMapper.toDomain(springDataOrderRepository.save(entity))
    }

    private fun findUserOrThrow(userId: UUID) = userRepository.findByIdOrNull(userId)
        ?: throw UserNotFoundException("User with id $userId not found")
}
