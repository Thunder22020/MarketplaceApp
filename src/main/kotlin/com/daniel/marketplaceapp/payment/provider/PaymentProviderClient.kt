package com.daniel.marketplaceapp.payment.provider

import com.daniel.marketplaceapp.payment.dto.CreatePaymentProviderRequest
import com.daniel.marketplaceapp.payment.dto.PaymentProviderResponse
import com.daniel.marketplaceapp.yookassa.payment.PaymentResponse

interface PaymentProviderClient {
    fun createPayment(req: CreatePaymentProviderRequest): PaymentProviderResponse
    fun cancelPayment(paymentId: String): PaymentResponse
    fun getPaymentById(paymentId: String): PaymentResponse
}
