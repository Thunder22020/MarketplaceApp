package com.daniel.marketplaceapp.order.repository

import com.daniel.marketplaceapp.order.entity.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderItemRepository : JpaRepository<OrderItem, UUID>
