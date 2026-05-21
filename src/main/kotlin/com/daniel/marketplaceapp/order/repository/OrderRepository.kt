package com.daniel.marketplaceapp.order.repository

import com.daniel.marketplaceapp.order.domain.Order
import java.util.UUID

interface OrderRepository {
    fun save(order: Order): Order
    fun findDraftByCustomerId(customerId: UUID): Order?
    fun findById(id: UUID): Order?
}
