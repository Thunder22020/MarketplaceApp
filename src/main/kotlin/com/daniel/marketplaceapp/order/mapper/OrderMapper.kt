package com.daniel.marketplaceapp.order.mapper

import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.dto.OrderItemResponse
import com.daniel.marketplaceapp.order.entity.OrderEntity
import com.daniel.marketplaceapp.order.entity.OrderItemEntity
import com.daniel.marketplaceapp.product.entity.ProductEntity
import com.daniel.marketplaceapp.user.entity.User
import java.util.UUID

object OrderMapper {
    fun toDomain(entity: OrderEntity) = Order(
        id = entity.id,
        status = entity.status,
        totalAmount = entity.totalAmount.copy(),
        customerId = requireNotNull(entity.customer.id),
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        items = entity.items.map { itemToDomain(it) }.toMutableList(),
        version = entity.version
    )

    fun toEntity(
        domain: Order,
        customer: User,
        productsByIds: Map<UUID, ProductEntity>
    ): OrderEntity {
        val orderEntity = OrderEntity(
            id = domain.id,
            status = domain.status,
            totalAmount = domain.totalAmount.copy(),
            customer = customer,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            version = domain.version
        )

        orderEntity.items = domain.items.map {
            val product = requireNotNull(productsByIds[it.productId]) {
                "Product ${it.productId} was not found for order item ${it.id}"
            }
            itemToEntity(it, orderEntity, product)
        }.toMutableList()

        return orderEntity
    }

    fun itemToDomain(entity: OrderItemEntity) = OrderItem(
        id = entity.id,
        orderId = requireNotNull(entity.order?.id),
        productId = requireNotNull(entity.product.id),
        unitPrice = entity.unitPrice.copy(),
        quantity = entity.quantity
    )

    fun itemToEntity(
        domain: OrderItem,
        order: OrderEntity,
        product: ProductEntity
    ) = OrderItemEntity(
        id = domain.id,
        order = order,
        product = product,
        unitPrice = domain.unitPrice.copy(),
        quantity = domain.quantity
    )

    fun toResponse(item: OrderItem) = OrderItemResponse(
        id = requireNotNull(item.id),
        orderId = requireNotNull(item.orderId),
        productId = requireNotNull(item.productId),
        unitPrice = item.unitPrice.amount,
        quantity = item.quantity,
    )
}
