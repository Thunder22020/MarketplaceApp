package com.daniel.marketplaceapp.payment.domain

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import java.time.Instant
import java.util.UUID

class Payment(
    var id: UUID?,
    val orderId: UUID,
    val externalId: UUID?,
    val confirmationUrl: String?,
    val amount: Money,
    var status: PaymentStatus,
    val createdAt: Instant,
    var updatedAt: Instant?,
)
