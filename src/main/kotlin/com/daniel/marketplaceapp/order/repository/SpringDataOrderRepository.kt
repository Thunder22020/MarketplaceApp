package com.daniel.marketplaceapp.order.repository

import com.daniel.marketplaceapp.order.entity.OrderEntity
import com.daniel.marketplaceapp.order.enums.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SpringDataOrderRepository : JpaRepository<OrderEntity, UUID> {
    @Query(
        """
        SELECT DISTINCT o FROM OrderEntity o
        LEFT JOIN FETCH o.items i
        WHERE o.customer.id = :customerId
            AND o.status = :status
    """
    )
    fun findDraftByCustomerIdWithItems(
        customerId: UUID,
        status: OrderStatus = OrderStatus.DRAFT
    ): OrderEntity?
}
