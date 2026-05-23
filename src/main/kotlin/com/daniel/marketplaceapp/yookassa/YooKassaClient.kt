package com.daniel.marketplaceapp.yookassa

import com.daniel.marketplaceapp.yookassa.payment.CreatePaymentRequest
import com.daniel.marketplaceapp.yookassa.payment.PaymentResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class YooKassaClient(
    private val client: HttpClient,
) {
    suspend fun createPayment(req: CreatePaymentRequest): PaymentResponse {
        val idempotencyKey = UUID.randomUUID().toString()
        val response: PaymentResponse = client.post(API_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("Idempotence-Key", idempotencyKey)
            setBody(req)
        }.body()
        return response
    }

    suspend fun cancelPayment(paymentId: String): PaymentResponse {
        val cancelEndpoint = "$API_ENDPOINT/$paymentId/cancel"
        val idempotencyKey = UUID.randomUUID().toString()
        val response: PaymentResponse = client.post(cancelEndpoint) {
            contentType(ContentType.Application.Json)
            header("Idempotence-Key", idempotencyKey)
        }.body()
        return response
    }

    suspend fun getPaymentById(paymentId: String) =
        client.get("$API_ENDPOINT/$paymentId").body<PaymentResponse>()

    fun close() {
        client.close()
    }

    companion object {
        private const val API_ENDPOINT = "https://api.yookassa.ru/v3/payments"
    }
}
