package com.daniel.marketplaceapp.yookassa.payment

data class PaymentMethod(
    val type: String,
    val id: String,
    val saved: Boolean,
    val title: String? = null,
)
