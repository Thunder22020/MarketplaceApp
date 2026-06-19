package com.daniel.marketplaceapp.payment.outbox

import java.time.Instant
import java.util.UUID

class PaymentOutboxEvent(
    var id: UUID?,
    val aggregateId: UUID,
    val eventType: String,
    val payload: String,
    var status: PaymentOutboxEventStatus,
    var attempts: Int,
    var lastError: String?,
    val createdAt: Instant,
    var publishedAt: Instant?,
) {
    fun markPublished() {
        status = PaymentOutboxEventStatus.PUBLISHED
        publishedAt = Instant.now()
        lastError = null
    }

    fun markFailed(error: String) {
        lastError = error.take(1000)
        status = PaymentOutboxEventStatus.FAILED
        attempts += 1
    }
}
