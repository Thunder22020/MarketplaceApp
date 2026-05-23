package com.daniel.marketplaceapp.payment.provider

import com.daniel.marketplaceapp.payment.dto.CreatePaymentProviderRequest
import com.daniel.marketplaceapp.payment.dto.PaymentProviderResponse
import com.daniel.marketplaceapp.yookassa.YooKassaClient
import com.daniel.marketplaceapp.yookassa.payment.Amount
import com.daniel.marketplaceapp.yookassa.payment.ConfirmationRequest
import com.daniel.marketplaceapp.yookassa.payment.CreatePaymentRequest
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class YooKassaPaymentProviderClient(
    private val yooKassaClient: YooKassaClient
) : PaymentProviderClient {
    override fun createPayment(req: CreatePaymentProviderRequest): PaymentProviderResponse {
        val yooKassaRequest = CreatePaymentRequest(
            amount = Amount(
                value = req.amount.amount.toPlainString(),
                currency = "RUB"
            ),
            confirmation = ConfirmationRequest(
                type = "redirect"
            ),
            description = req.description,
            capture = true
        )
        val result = runBlocking {
            yooKassaClient.createPayment(yooKassaRequest)
        }

        return PaymentProviderResponse(
            externalId = result.id,
            confirmationUrl = requireNotNull(result.confirmation?.confirmationUrl)
        )
    }

    override fun cancelPayment(paymentId: String) = runBlocking {
        yooKassaClient.cancelPayment(paymentId)
    }

    override fun getPaymentById(paymentId: String) = runBlocking {
        yooKassaClient.getPaymentById(paymentId)
    }
}
