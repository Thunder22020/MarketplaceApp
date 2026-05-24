package com.daniel.marketplaceapp.balance.domain

import com.daniel.marketplaceapp.balance.enums.BalanceTransactionType
import com.daniel.marketplaceapp.core.domain.Money
import java.time.Instant
import java.util.UUID

class BalanceTransaction(
    var id: UUID?,
    val userId: UUID,
    val orderId: UUID,
    val paymentId: UUID,
    val type: BalanceTransactionType,
    val amount: Money,
    val createdAt: Instant,
)
