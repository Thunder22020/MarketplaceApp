package com.daniel.marketplaceapp.yookassa.payment

data class ConfirmationResponse(
    val type: String,
    val returnUrl: String = "https://www.google.com/",
    val confirmationUrl: String? = null,
)
