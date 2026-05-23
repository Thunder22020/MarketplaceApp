package com.daniel.marketplaceapp.yookassa.payment

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class PaymentResponse(
    val id: String,
    val status: YooKassaPaymentStatus,
    val amount: Amount,
    val incomeAmount: Amount? = null,
    val description: String? = null,
    val recipient: Recipient? = null,
    @JsonProperty("payment_method")
    val paymentMethod: PaymentMethod? = null,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("expires_at")
    val expiresAt: Instant? = null,
    val confirmation: ConfirmationResponse? = null,
    val paid: Boolean,
    val refunded: Boolean,
    val test: Boolean,
    val metadata: Map<String, String> = emptyMap(),
    @JsonProperty("cancellation_details")
    val cancellationDetails: CancellationDetails? = null
)
