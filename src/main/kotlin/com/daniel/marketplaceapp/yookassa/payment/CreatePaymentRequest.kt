package com.daniel.marketplaceapp.yookassa.payment

data class CreatePaymentRequest(
    val amount: Amount,
    val confirmation: ConfirmationRequest,
    val capture: Boolean,
    val description: String? = null,
)
