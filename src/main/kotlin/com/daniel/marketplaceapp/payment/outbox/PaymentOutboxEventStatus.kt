package com.daniel.marketplaceapp.payment.outbox

enum class PaymentOutboxEventStatus {
    NEW,
    PUBLISHED,
    FAILED
}
