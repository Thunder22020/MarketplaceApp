package com.daniel.marketplaceapp.payment.service

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.repository.OrderRepository
import com.daniel.marketplaceapp.payment.domain.Payment
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import com.daniel.marketplaceapp.payment.repository.PaymentRepository
import com.daniel.marketplaceapp.testsupport.annotations.ServiceIntegrationTest
import com.daniel.marketplaceapp.testsupport.fixtures.randomUrl
import com.daniel.marketplaceapp.testsupport.steps.OrderSteps
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import com.daniel.marketplaceapp.yookassa.payment.Amount
import com.daniel.marketplaceapp.yookassa.payment.PaymentResponse
import com.daniel.marketplaceapp.yookassa.payment.YooKassaPaymentStatus
import com.daniel.marketplaceapp.yookassa.webhook.dto.YooKassaWebhookRequest
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

@ServiceIntegrationTest
class PaymentWebhookServiceIntegrationTest {
    @Autowired
    private lateinit var paymentWebhookService: PaymentWebhookService

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderSteps: OrderSteps

    @Autowired
    private lateinit var userSteps: UserSteps

    private lateinit var customerId: UUID

    @BeforeAll
    fun setUp() {
        customerId = userSteps.createUser().id!!
    }

    @Test
    fun `should mark payment succeeded and order paid on succeeded webhook`() {
        val order = orderSteps.createOrder(
            customerId = customerId,
            status = OrderStatus.PENDING_PAYMENT
        )
        val payment = createPayment(orderId = order.id!!)

        paymentWebhookService.handleYooKassaWebhook(
            createWebhookRequest(
                externalId = payment.externalId!!,
                event = PAYMENT_SUCCEEDED_EVENT,
                status = YooKassaPaymentStatus.SUCCEEDED
            )
        )

        val updatedPayment = paymentRepository.findById(payment.id!!)
            .shouldNotBeNull()
        val updatedOrder = orderRepository.findById(order.id!!)
            .shouldNotBeNull()

        updatedPayment.status shouldBe PaymentStatus.SUCCEEDED
        updatedPayment.updatedAt.shouldNotBeNull()
        updatedOrder.status shouldBe OrderStatus.PAID
        updatedOrder.updatedAt.shouldNotBeNull()
    }

    @Test
    fun `should ignore duplicate succeeded webhook`() {
        val order = orderSteps.createOrder(
            customerId = customerId,
            status = OrderStatus.PAID
        )
        val payment = createPayment(
            orderId = order.id!!,
            status = PaymentStatus.SUCCEEDED
        )

        paymentWebhookService.handleYooKassaWebhook(
            createWebhookRequest(
                externalId = payment.externalId!!,
                event = PAYMENT_SUCCEEDED_EVENT,
                status = YooKassaPaymentStatus.SUCCEEDED
            )
        )

        val updatedPayment = paymentRepository.findById(payment.id!!)
            .shouldNotBeNull()
        val updatedOrder = orderRepository.findById(order.id!!)
            .shouldNotBeNull()

        updatedPayment.status shouldBe PaymentStatus.SUCCEEDED
        updatedOrder.status shouldBe OrderStatus.PAID
    }

    @Test
    fun `should mark payment failed and order failed on canceled webhook`() {
        val order = orderSteps.createOrder(
            customerId = customerId,
            status = OrderStatus.PENDING_PAYMENT
        )
        val payment = createPayment(orderId = order.id!!)

        paymentWebhookService.handleYooKassaWebhook(
            createWebhookRequest(
                externalId = payment.externalId!!,
                event = PAYMENT_CANCELED_EVENT,
                status = YooKassaPaymentStatus.CANCELED
            )
        )

        val updatedPayment = paymentRepository.findById(payment.id!!)
            .shouldNotBeNull()
        val updatedOrder = orderRepository.findById(order.id!!)
            .shouldNotBeNull()

        updatedPayment.status shouldBe PaymentStatus.FAILED
        updatedPayment.updatedAt.shouldNotBeNull()
        updatedOrder.status shouldBe OrderStatus.FAILED
        updatedOrder.updatedAt.shouldNotBeNull()
    }

    @Test
    fun `should ignore duplicate canceled webhook`() {
        val order = orderSteps.createOrder(
            customerId = customerId,
            status = OrderStatus.FAILED
        )
        val payment = createPayment(
            orderId = order.id!!,
            status = PaymentStatus.FAILED
        )

        paymentWebhookService.handleYooKassaWebhook(
            createWebhookRequest(
                externalId = payment.externalId!!,
                event = PAYMENT_CANCELED_EVENT,
                status = YooKassaPaymentStatus.CANCELED
            )
        )

        val updatedPayment = paymentRepository.findById(payment.id!!)
            .shouldNotBeNull()
        val updatedOrder = orderRepository.findById(order.id!!)
            .shouldNotBeNull()

        updatedPayment.status shouldBe PaymentStatus.FAILED
        updatedOrder.status shouldBe OrderStatus.FAILED
    }

    private fun createPayment(
        orderId: UUID,
        status: PaymentStatus = PaymentStatus.PENDING,
        externalId: String = UUID.randomUUID().toString(),
    ) = paymentRepository.save(
        Payment(
            id = null,
            orderId = orderId,
            externalId = externalId,
            confirmationUrl = randomUrl() + "/$externalId",
            amount = Money(BigDecimal("100.00")),
            status = status,
            createdAt = Instant.now(),
            updatedAt = null,
        )
    )

    private fun createWebhookRequest(
        externalId: String,
        event: String,
        status: YooKassaPaymentStatus,
    ) = YooKassaWebhookRequest(
        type = "notification",
        event = event,
        `object` = PaymentResponse(
            id = externalId,
            status = status,
            amount = Amount(
                value = "100.00",
                currency = "RUB"
            ),
            createdAt = Instant.now(),
            paid = status == YooKassaPaymentStatus.SUCCEEDED,
            refunded = false,
            test = true,
        )
    )

    companion object {
        private const val PAYMENT_SUCCEEDED_EVENT = "payment.succeeded"
        private const val PAYMENT_CANCELED_EVENT = "payment.canceled"
    }
}
