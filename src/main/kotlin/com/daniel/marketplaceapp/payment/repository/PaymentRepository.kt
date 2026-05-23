package com.daniel.marketplaceapp.payment.repository

import com.daniel.marketplaceapp.payment.domain.Payment
import java.util.UUID

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun findById(id: UUID): Payment?
    fun findByOrderId(orderId: UUID): Payment?
    fun findByExternalId(externalId: String): Payment?
}
