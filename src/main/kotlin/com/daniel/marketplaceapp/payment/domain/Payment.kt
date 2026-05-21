package com.daniel.marketplaceapp.payment.domain

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import java.time.Instant
import java.util.UUID

class Payment(
    var id: UUID?,
    val orderId: UUID,
    var externalId: String?,
    var confirmationUrl: String?,
    val amount: Money,
    var status: PaymentStatus,
    val createdAt: Instant,
    var updatedAt: Instant?,
) {
    fun markPending(confirmationUrl: String, externalId: String) {
        this.confirmationUrl = confirmationUrl
        this.externalId = externalId
        this.status = PaymentStatus.PENDING
        setUpdatedAt()
    }

    fun markFailed() {
        this.status = PaymentStatus.FAILED
        setUpdatedAt()
    }

    private fun setUpdatedAt() {
        this.updatedAt = Instant.now()
    }
}
