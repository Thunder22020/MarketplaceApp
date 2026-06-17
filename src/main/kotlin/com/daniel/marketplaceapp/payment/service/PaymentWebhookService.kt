package com.daniel.marketplaceapp.payment.service

import com.daniel.marketplaceapp.balance.service.BalanceTransactionService
import com.daniel.marketplaceapp.core.event.payment.PaymentSucceededEvent
import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.exception.OrderNotFoundException
import com.daniel.marketplaceapp.order.repository.OrderRepository
import com.daniel.marketplaceapp.payment.domain.Payment
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import com.daniel.marketplaceapp.payment.exception.PaymentNotFoundException
import com.daniel.marketplaceapp.payment.repository.PaymentRepository
import com.daniel.marketplaceapp.yookassa.payment.YooKassaPaymentStatus
import com.daniel.marketplaceapp.yookassa.webhook.dto.YooKassaWebhookRequest
import java.time.Duration
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentWebhookService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val redisTemplate: StringRedisTemplate,
    private val kafkaTemplate: KafkaTemplate<String, PaymentSucceededEvent>
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handleYooKassaWebhook(request: YooKassaWebhookRequest) {
        val redisKey = getRedisKey(request)
        if (redisTemplate.opsForValue().get(redisKey) != null) {
            log.info(
                "Cache hit for YooKassa webhook event {}, external payment id {}",
                request.event,
                request.`object`.id
            )
            return
        }
        val payment = findPaymentByExternalIdOrThrow(request.`object`.id)
        val order = findOrderByIdOrThrow(payment.orderId)
        resolveWebhook(request, payment, order)
    }

    private fun resolveWebhook(
        request: YooKassaWebhookRequest,
        payment: Payment,
        order: Order
    ) {
        when {
            isSuccessfulPayment(request) && payment.isSucceeded() ->
                logRetrySuccessWebhook(payment)
            isSuccessfulPayment(request) && payment.isPending() ->
                processSuccessfulPayment(payment, order, request)
            isCanceledPayment(request) && payment.isPending() ->
                processCanceledPayment(payment, order, request)
            isCanceledPayment(request) && payment.isFailed() ->
                logRetryCanceledWebhook(payment)
            else -> logIgnoredWebhook(request, payment)
        }
    }

    private fun processSuccessfulPayment(
        payment: Payment,
        order: Order,
        req: YooKassaWebhookRequest
    ) {
        log.info("Processing successful YooKassa payment. paymentId={}", payment.id)

        payment.markSucceeded()
        order.markPaid()

        paymentRepository.save(payment)
        orderRepository.save(order)

        val event = PaymentSucceededEvent(
            order.id!!,
            payment.id!!,
            order.items
        )
        kafkaTemplate.send("payment.succeeded", event)

        cacheRequest(req)
    }

    private fun processCanceledPayment(
        payment: Payment,
        order: Order,
        req: YooKassaWebhookRequest
    ) {
        log.info("Processing canceled YooKassa payment. paymentId={}", payment.id)

        payment.markFailed()
        order.markFailed()

        paymentRepository.save(payment)
        orderRepository.save(order)

        cacheRequest(req)
    }

    private fun logRetrySuccessWebhook(payment: Payment) {
        log.info(
            "Ignoring duplicate successful webhook. paymentId={}",
            payment.id
        )
    }

    private fun logRetryCanceledWebhook(payment: Payment) {
        log.info(
            "Ignoring duplicate canceled webhook. paymentId={}",
            payment.id
        )
    }

    private fun logIgnoredWebhook(
        request: YooKassaWebhookRequest,
        payment: Payment,
    ) {
        log.warn(
            "Ignoring unsupported webhook state. event={}, yookassaStatus={}, paymentStatus={}, paymentId={}",
            request.event,
            request.`object`.status,
            payment.status,
            payment.id,
        )
    }

    private fun findPaymentByExternalIdOrThrow(externalId: String): Payment =
        paymentRepository.findByExternalId(externalId)
            ?: throw PaymentNotFoundException("Payment not found by external id: $externalId")

    private fun findOrderByIdOrThrow(orderId: UUID): Order =
        orderRepository.findById(orderId)
            ?: throw OrderNotFoundException("Order not found by id: $orderId")

    private fun isSuccessfulPayment(request: YooKassaWebhookRequest): Boolean =
        request.event == PAYMENT_SUCCEEDED_EVENT &&
                request.`object`.status == YooKassaPaymentStatus.SUCCEEDED

    private fun isCanceledPayment(request: YooKassaWebhookRequest): Boolean =
        request.event == PAYMENT_CANCELED_EVENT &&
                request.`object`.status == YooKassaPaymentStatus.CANCELED

    private fun Payment.isPending(): Boolean =
        status == PaymentStatus.PENDING

    private fun Payment.isSucceeded(): Boolean =
        status == PaymentStatus.SUCCEEDED

    private fun Payment.isFailed(): Boolean =
        status == PaymentStatus.FAILED

    private fun getRedisKey(req: YooKassaWebhookRequest) =
        "webhook:yookassa:${req.event}:${req.`object`.id}"

    private fun cacheRequest(req: YooKassaWebhookRequest) {
        val redisKey = getRedisKey(req)
        redisTemplate.opsForValue().set(redisKey, "1", Duration.ofHours(1))
    }

    companion object {
        private const val PAYMENT_SUCCEEDED_EVENT = "payment.succeeded"
        private const val PAYMENT_CANCELED_EVENT = "payment.canceled"
    }
}
