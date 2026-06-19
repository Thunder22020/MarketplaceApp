package com.daniel.marketplaceapp.payment.outbox

import java.util.UUID

interface PaymentOutboxEventRepository {
    fun save(event: PaymentOutboxEvent): PaymentOutboxEvent

    fun saveAll(events: Collection<PaymentOutboxEvent>): List<PaymentOutboxEvent>

    fun findReadyForPublish(
        limit: Int,
        maxAttempts: Int
    ): List<PaymentOutboxEvent>

    fun findByAggregateIdAndEventType(
        aggregateId: UUID,
        eventType: String
    ): PaymentOutboxEvent?
}
