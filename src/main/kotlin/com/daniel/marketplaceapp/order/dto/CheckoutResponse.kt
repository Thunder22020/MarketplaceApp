package com.daniel.marketplaceapp.order.dto

data class CheckoutResponse(
    val order: OrderResponse,
    val payment: PaymentCheckoutResponse,
)
