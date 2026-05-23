package com.daniel.marketplaceapp.yookassa.webhook.controller

import com.daniel.marketplaceapp.payment.service.PaymentWebhookService
import com.daniel.marketplaceapp.yookassa.webhook.dto.YooKassaWebhookRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class YooKassaWebhookController(
    private val paymentWebhookService: PaymentWebhookService
) {
    @PostMapping("/webhooks/yookassa")
    fun getWebhookEvent(
        @RequestBody request: YooKassaWebhookRequest
    ): ResponseEntity<Void> {
        paymentWebhookService.handleYooKassaWebhook(request)
        return ResponseEntity.ok().build()
    }
}
