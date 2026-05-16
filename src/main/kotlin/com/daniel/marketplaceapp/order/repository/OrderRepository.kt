package com.daniel.marketplaceapp.order.repository

import com.daniel.marketplaceapp.order.domain.Order
import java.util.UUID

interface OrderRepository {
    fun findDraftByCustomerId(customerId: UUID): Order?
    fun save(order: Order): Order
}
