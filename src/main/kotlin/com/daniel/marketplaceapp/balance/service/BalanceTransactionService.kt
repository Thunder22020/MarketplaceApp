package com.daniel.marketplaceapp.balance.service

import com.daniel.marketplaceapp.balance.domain.BalanceTransaction
import com.daniel.marketplaceapp.balance.enums.BalanceTransactionType
import com.daniel.marketplaceapp.balance.repository.BalanceTransactionRepository
import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.payment.domain.Payment
import java.time.Instant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BalanceTransactionService(
    private val repository: BalanceTransactionRepository
) {
    @Transactional
    fun creditSellersForPaidOrder(order: Order, payment: Payment) {
        if (repository.existsByPaymentIdAndType(payment.id!!, BalanceTransactionType.CREDIT)) {
            return
        }

        val amountBySellerId = order.items
            .groupBy { it.sellerId }
            .mapValues { (_, items) ->
                items.fold(Money.ZERO) { acc, item -> acc + item.totalPrice() }
            }

        val transactions = amountBySellerId.map { (sellerId, amount) ->
            BalanceTransaction(
                id = null,
                userId = sellerId,
                orderId = requireNotNull(order.id),
                paymentId = requireNotNull(payment.id),
                type = BalanceTransactionType.CREDIT,
                amount = amount,
                createdAt = Instant.now(),
            )
        }
        repository.saveAll(transactions)
    }
}
