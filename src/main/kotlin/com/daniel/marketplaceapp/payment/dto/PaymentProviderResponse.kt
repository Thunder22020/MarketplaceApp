package com.daniel.marketplaceapp.payment.dto

data class PaymentProviderResponse(
    val externalId: String,
    val confirmationUrl: String,
)
