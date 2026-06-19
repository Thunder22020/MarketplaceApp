package com.daniel.marketplaceapp.payment.outbox

import com.daniel.marketplaceapp.core.mapper.toLocalDateTime
import com.daniel.marketplaceapp.jooq.Tables.PAYMENT_OUTBOX_EVENTS
import com.daniel.marketplaceapp.payment.mapper.toPaymentOutboxEventDomain
import java.util.UUID
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class JooqPaymentOutboxEventRepository(
    private val dsl: DSLContext
): PaymentOutboxEventRepository {
    override fun findByAggregateIdAndEventType(
        aggregateId: UUID,
        eventType: String
    ) = dsl.selectFrom(PAYMENT_OUTBOX_EVENTS)
            .where(PAYMENT_OUTBOX_EVENTS.AGGREGATE_ID.eq(aggregateId)
                .and(PAYMENT_OUTBOX_EVENTS.EVENT_TYPE.eq(eventType)))
            .fetchOne()
            ?.map { it.toPaymentOutboxEventDomain() }

    override fun findReadyForPublish(limit: Int, maxAttempts: Int) =
        dsl.selectFrom(PAYMENT_OUTBOX_EVENTS)
            .where(
                PAYMENT_OUTBOX_EVENTS.ATTEMPTS.lessThan(maxAttempts)
                .and(PAYMENT_OUTBOX_EVENTS.STATUS.`in`(
                    PaymentOutboxEventStatus.NEW.toString(),
                    PaymentOutboxEventStatus.FAILED.toString()))
            )
            .limit(limit)
            .fetch()
            .map { it.toPaymentOutboxEventDomain() }

    override fun saveAll(events: Collection<PaymentOutboxEvent>): List<PaymentOutboxEvent> {
        val queries = events.map { event ->
            if (event.id == null) {
                val id = UUID.randomUUID()
                event.id = id
                getInsertOneQuery(event, id)
            } else {
                getUpdateOneQuery(event)
            }
        }
        dsl.batch(queries).execute()
        return events.toList()
    }

    override fun save(event: PaymentOutboxEvent) =
        if (event.id == null) {
            insert(event)
        } else {
            update(event)
        }

    private fun insert(event: PaymentOutboxEvent): PaymentOutboxEvent {
        val id = UUID.randomUUID()
        getInsertOneQuery(event, id).execute()
        event.id = id
        return event
    }

    private fun update(event: PaymentOutboxEvent): PaymentOutboxEvent {
        getUpdateOneQuery(event).execute()
        return event
    }

    private fun getInsertOneQuery(event: PaymentOutboxEvent, id: UUID) =
        dsl.insertInto(PAYMENT_OUTBOX_EVENTS)
            .set(PAYMENT_OUTBOX_EVENTS.ID, id)
            .set(PAYMENT_OUTBOX_EVENTS.STATUS, event.status.toString())
            .set(PAYMENT_OUTBOX_EVENTS.EVENT_TYPE, event.eventType)
            .set(PAYMENT_OUTBOX_EVENTS.PAYLOAD, event.payload)
            .set(PAYMENT_OUTBOX_EVENTS.AGGREGATE_ID, event.aggregateId)
            .set(PAYMENT_OUTBOX_EVENTS.ATTEMPTS, event.attempts)
            .set(PAYMENT_OUTBOX_EVENTS.LAST_ERROR, event.lastError)
            .set(PAYMENT_OUTBOX_EVENTS.PUBLISHED_AT, event.publishedAt?.toLocalDateTime())
            .set(PAYMENT_OUTBOX_EVENTS.CREATED_AT, event.createdAt.toLocalDateTime())

    private fun getUpdateOneQuery(event: PaymentOutboxEvent) =
        dsl.update(PAYMENT_OUTBOX_EVENTS)
            .set(PAYMENT_OUTBOX_EVENTS.STATUS, event.status.toString())
            .set(PAYMENT_OUTBOX_EVENTS.ATTEMPTS, event.attempts)
            .set(PAYMENT_OUTBOX_EVENTS.LAST_ERROR, event.lastError)
            .set(PAYMENT_OUTBOX_EVENTS.PUBLISHED_AT, event.publishedAt?.toLocalDateTime())
            .where(PAYMENT_OUTBOX_EVENTS.ID.eq(event.id))
}
