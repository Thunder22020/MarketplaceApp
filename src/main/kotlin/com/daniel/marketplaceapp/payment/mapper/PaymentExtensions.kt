package com.daniel.marketplaceapp.payment.mapper

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.core.mapper.toInstantUtc
import com.daniel.marketplaceapp.jooq.Tables.PAYMENTS
import com.daniel.marketplaceapp.payment.domain.Payment
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import org.jooq.Record

fun Record.toDomain() = Payment(
    id = requireNotNull(get(PAYMENTS.ID)),
    orderId = requireNotNull(get(PAYMENTS.ORDER_ID)),
    externalId = get(PAYMENTS.EXTERNAL_ID),
    confirmationUrl = get(PAYMENTS.CONFIRMATION_URL),
    amount = Money(requireNotNull(get(PAYMENTS.AMOUNT))),
    status = PaymentStatus.valueOf(requireNotNull(get(PAYMENTS.STATUS))),
    createdAt = requireNotNull(get(PAYMENTS.CREATED_AT)).toInstantUtc(),
    updatedAt = get(PAYMENTS.UPDATED_AT)?.toInstantUtc()
)
