package com.daniel.marketplaceapp.balance.repository

import com.daniel.marketplaceapp.balance.domain.BalanceTransaction
import com.daniel.marketplaceapp.balance.enums.BalanceTransactionType
import java.util.UUID

interface BalanceTransactionRepository {
    fun save(bt: BalanceTransaction): BalanceTransaction
    fun saveAll(transactions: List<BalanceTransaction>): List<BalanceTransaction>
    fun existsByPaymentIdAndType(paymentId: UUID, type: BalanceTransactionType): Boolean
    fun findAllByPaymentIdAndType(paymentId: UUID, type: BalanceTransactionType): List<BalanceTransaction>
}
