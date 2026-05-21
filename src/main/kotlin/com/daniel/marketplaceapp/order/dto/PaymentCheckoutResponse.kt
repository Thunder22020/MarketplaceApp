package com.daniel.marketplaceapp.order.dto

data class PaymentCheckoutResponse(
    val externalPaymentId: String,
    val confirmationUrl: String,
)
