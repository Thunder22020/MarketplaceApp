package com.daniel.marketplaceapp.payment.provider

import com.daniel.marketplaceapp.payment.dto.CreatePaymentProviderRequest
import com.daniel.marketplaceapp.payment.dto.PaymentProviderResponse

interface PaymentProviderClient {
    fun createPayment(req: CreatePaymentProviderRequest): PaymentProviderResponse
}
