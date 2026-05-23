package com.daniel.marketplaceapp.yookassa.webhook.dto

import com.daniel.marketplaceapp.yookassa.payment.PaymentResponse

data class YooKassaWebhookRequest(
    val type: String,
    val event: String,
    val `object`: PaymentResponse,
)
