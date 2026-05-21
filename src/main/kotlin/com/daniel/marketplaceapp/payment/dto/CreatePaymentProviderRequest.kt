package com.daniel.marketplaceapp.payment.dto

import com.daniel.marketplaceapp.core.domain.Money

data class CreatePaymentProviderRequest(
    val amount: Money,
    val description: String,
)
