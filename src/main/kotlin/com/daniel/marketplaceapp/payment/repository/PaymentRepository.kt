package com.daniel.marketplaceapp.payment.repository

import com.daniel.marketplaceapp.payment.domain.Payment

interface PaymentRepository {
    fun save(payment: Payment): Payment
}
