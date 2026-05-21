package com.daniel.marketplaceapp.yookassa.payment

data class ConfirmationRequest(
    val type: String,
    val returnUrl: String = "https://www.google.com/",
)
