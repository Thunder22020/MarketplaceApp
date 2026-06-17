package com.daniel.marketplaceapp.balance.service

import com.daniel.marketplaceapp.balance.domain.BalanceTransaction
import com.daniel.marketplaceapp.balance.enums.BalanceTransactionType
import com.daniel.marketplaceapp.balance.repository.BalanceTransactionRepository
import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.core.event.payment.PaymentSucceededEvent
import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.payment.domain.Payment
import java.time.Instant
import java.util.UUID
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BalanceTransactionService(
    private val repository: BalanceTransactionRepository
) {
    @Transactional
    @KafkaListener(topics = ["payment.succeeded"])
    fun creditSellersForPaidOrder(event: PaymentSucceededEvent) {
        if (repository.existsByPaymentIdAndType(event.paymentId, BalanceTransactionType.CREDIT)) {
            return
        }

        val amountBySellerId = event.orderItems
            .groupBy { it.sellerId }
            .mapValues { (_, items) ->
                items.fold(Money.ZERO) { acc, item -> acc + item.totalPrice() }
            }

        val transactions = amountBySellerId.map { (sellerId, amount) ->
            BalanceTransaction(
                id = null,
                userId = sellerId,
                orderId = requireNotNull(event.orderId),
                paymentId = event.paymentId,
                type = BalanceTransactionType.CREDIT,
                amount = amount,
                createdAt = Instant.now(),
            )
        }
        repository.saveAll(transactions)
    }
}
