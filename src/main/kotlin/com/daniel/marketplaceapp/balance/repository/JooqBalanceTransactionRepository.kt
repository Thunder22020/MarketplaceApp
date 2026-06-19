package com.daniel.marketplaceapp.balance.repository

import com.daniel.marketplaceapp.balance.domain.BalanceTransaction
import com.daniel.marketplaceapp.balance.enums.BalanceTransactionType
import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.core.mapper.toInstantUtc
import com.daniel.marketplaceapp.core.mapper.toLocalDateTime
import com.daniel.marketplaceapp.jooq.Tables.BALANCE_TRANSACTIONS
import java.util.UUID
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository

@Repository
class JooqBalanceTransactionRepository(
    private val dsl: DSLContext
): BalanceTransactionRepository {
    override fun save(bt: BalanceTransaction): BalanceTransaction {
        val id = UUID.randomUUID()
        getInsertOneQuery(bt, id).execute()
        bt.id = id
        return bt
    }

    override fun saveAll(transactions: List<BalanceTransaction>): List<BalanceTransaction> {
        val queries = transactions.map { bt ->
            val id = UUID.randomUUID()
            bt.id = id
            getInsertOneQuery(bt, id)
        }
        dsl.batch(queries).execute()
        return transactions
    }

    private fun getInsertOneQuery(bt: BalanceTransaction, id: UUID) =
        dsl.insertInto(BALANCE_TRANSACTIONS)
            .set(BALANCE_TRANSACTIONS.ID, id)
            .set(BALANCE_TRANSACTIONS.USER_ID, bt.userId)
            .set(BALANCE_TRANSACTIONS.ORDER_ID, bt.orderId)
            .set(BALANCE_TRANSACTIONS.PAYMENT_ID, bt.paymentId)
            .set(BALANCE_TRANSACTIONS.TYPE, bt.type.name)
            .set(BALANCE_TRANSACTIONS.AMOUNT, bt.amount.amount)
            .set(BALANCE_TRANSACTIONS.CREATED_AT, bt.createdAt.toLocalDateTime())

    override fun existsByPaymentIdAndType(paymentId: UUID, type: BalanceTransactionType) =
        dsl.fetchExists(
            dsl.selectFrom(BALANCE_TRANSACTIONS)
                .where(
                    BALANCE_TRANSACTIONS.PAYMENT_ID.eq(paymentId)
                        .and(BALANCE_TRANSACTIONS.TYPE.eq(type.name))
                )
        )

    override fun findAllByPaymentIdAndType(
        paymentId: UUID,
        type: BalanceTransactionType
    ) = dsl.selectFrom(BALANCE_TRANSACTIONS)
        .where(BALANCE_TRANSACTIONS.PAYMENT_ID.eq(paymentId))
        .and(BALANCE_TRANSACTIONS.TYPE.eq(type.name))
        .fetch()
        .map { it.toDomain() }

    private fun Record.toDomain() = BalanceTransaction(
        id = requireNotNull(get(BALANCE_TRANSACTIONS.ID)),
        userId = requireNotNull(get(BALANCE_TRANSACTIONS.USER_ID)),
        orderId = requireNotNull(get(BALANCE_TRANSACTIONS.ORDER_ID)),
        paymentId = requireNotNull(get(BALANCE_TRANSACTIONS.PAYMENT_ID)),
        type = BalanceTransactionType.valueOf(requireNotNull(get(BALANCE_TRANSACTIONS.TYPE))),
        amount = Money(requireNotNull(get(BALANCE_TRANSACTIONS.AMOUNT))),
        createdAt = requireNotNull(get(BALANCE_TRANSACTIONS.CREATED_AT)).toInstantUtc(),
    )
}
