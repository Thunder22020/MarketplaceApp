package com.daniel.marketplaceapp.order.dto

import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.payment.domain.Payment

data class CheckoutResult(
    val order: Order,
    val payment: Payment
)
