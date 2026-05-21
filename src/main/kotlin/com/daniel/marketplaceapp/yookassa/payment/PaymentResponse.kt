package com.daniel.marketplaceapp.yookassa.payment

import java.time.Instant

data class PaymentResponse(
    val id: String,
    val status: YooKassaPaymentStatus,
    val amount: Amount,
    val incomeAmount: Amount? = null,
    val description: String? = null,
    val recipient: Recipient? = null,
    val paymentMethod: PaymentMethod? = null,
    val createdAt: Instant,
    val expiresAt: Instant? = null,
    val confirmation: ConfirmationResponse? = null,
    val paid: Boolean,
    val refunded: Boolean,
    val test: Boolean,
    val metadata: Map<String, String> = emptyMap(),
    val cancellationDetails: CancellationDetails? = null
)
