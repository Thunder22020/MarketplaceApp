package com.daniel.marketplaceapp.payment.mapper

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.core.mapper.toInstantUtc
import com.daniel.marketplaceapp.jooq.Tables.PAYMENTS
import com.daniel.marketplaceapp.jooq.Tables.PAYMENT_OUTBOX_EVENTS
import com.daniel.marketplaceapp.payment.domain.Payment
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import com.daniel.marketplaceapp.payment.outbox.PaymentOutboxEvent
import com.daniel.marketplaceapp.payment.outbox.PaymentOutboxEventStatus
import org.jooq.Record

fun Record.toPaymentDomain() = Payment(
    id = requireNotNull(get(PAYMENTS.ID)),
    orderId = requireNotNull(get(PAYMENTS.ORDER_ID)),
    externalId = get(PAYMENTS.EXTERNAL_ID),
    confirmationUrl = get(PAYMENTS.CONFIRMATION_URL),
    amount = Money(requireNotNull(get(PAYMENTS.AMOUNT))),
    status = PaymentStatus.valueOf(requireNotNull(get(PAYMENTS.STATUS))),
    createdAt = requireNotNull(get(PAYMENTS.CREATED_AT)).toInstantUtc(),
    updatedAt = get(PAYMENTS.UPDATED_AT)?.toInstantUtc()
)

fun Record.toPaymentOutboxEventDomain() = PaymentOutboxEvent(
    id = requireNotNull(get(PAYMENT_OUTBOX_EVENTS.ID)),
    aggregateId = requireNotNull(get(PAYMENT_OUTBOX_EVENTS.AGGREGATE_ID)),
    eventType = requireNotNull(get(PAYMENT_OUTBOX_EVENTS.EVENT_TYPE)),
    payload = requireNotNull(get(PAYMENT_OUTBOX_EVENTS.PAYLOAD)),
    status = PaymentOutboxEventStatus.valueOf(requireNotNull(get(PAYMENT_OUTBOX_EVENTS.STATUS))),
    attempts = requireNotNull(get(PAYMENT_OUTBOX_EVENTS.ATTEMPTS)),
    lastError = get(PAYMENT_OUTBOX_EVENTS.LAST_ERROR),
    createdAt = requireNotNull(get(PAYMENT_OUTBOX_EVENTS.CREATED_AT)).toInstantUtc(),
    publishedAt = get(PAYMENT_OUTBOX_EVENTS.PUBLISHED_AT)?.toInstantUtc()
)
