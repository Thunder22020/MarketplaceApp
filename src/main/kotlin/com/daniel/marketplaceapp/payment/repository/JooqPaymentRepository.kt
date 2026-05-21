package com.daniel.marketplaceapp.payment.repository

import com.daniel.marketplaceapp.core.mapper.toLocalDateTime
import com.daniel.marketplaceapp.jooq.Tables.PAYMENTS
import com.daniel.marketplaceapp.payment.domain.Payment
import com.daniel.marketplaceapp.payment.mapper.toDomain
import java.util.UUID
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class JooqPaymentRepository(
    private val dsl: DSLContext
): PaymentRepository {
    override fun findById(id: UUID) =
        dsl.selectFrom(PAYMENTS)
            .where(PAYMENTS.ID.eq(id))
            .fetchOne()
            ?.toDomain()

    override fun findByOrderId(orderId: UUID) =
        dsl.selectFrom(PAYMENTS)
            .where(PAYMENTS.ORDER_ID.eq(orderId))
            .fetchOne()
            ?.toDomain()

    override fun save(payment: Payment) =
        if (payment.id == null) {
            insert(payment)
        } else {
            update(payment)
        }

    private fun insert(payment: Payment): Payment {
        val paymentId = UUID.randomUUID()
        dsl.insertInto(PAYMENTS)
            .set(PAYMENTS.ID, paymentId)
            .set(PAYMENTS.STATUS, payment.status.name)
            .set(PAYMENTS.CONFIRMATION_URL, payment.confirmationUrl)
            .set(PAYMENTS.AMOUNT, payment.amount.amount)
            .set(PAYMENTS.ORDER_ID, payment.orderId)
            .set(PAYMENTS.EXTERNAL_ID, payment.externalId)
            .set(PAYMENTS.CREATED_AT, payment.createdAt.toLocalDateTime())
            .set(PAYMENTS.UPDATED_AT, payment.updatedAt?.toLocalDateTime())
            .execute()
        payment.id = paymentId
        return payment
    }

    private fun update(payment: Payment): Payment {
        dsl.update(PAYMENTS)
            .set(PAYMENTS.STATUS, payment.status.name)
            .set(PAYMENTS.CONFIRMATION_URL, payment.confirmationUrl)
            .set(PAYMENTS.EXTERNAL_ID, payment.externalId)
            .set(PAYMENTS.UPDATED_AT, payment.updatedAt?.toLocalDateTime())
            .where(PAYMENTS.ID.eq(payment.id))
            .execute()
        return payment
    }
}
