package com.daniel.marketplaceapp.payment.service

import com.daniel.marketplaceapp.balance.enums.BalanceTransactionType
import com.daniel.marketplaceapp.balance.repository.BalanceTransactionRepository
import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.repository.OrderRepository
import com.daniel.marketplaceapp.payment.domain.Payment
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import com.daniel.marketplaceapp.payment.repository.PaymentRepository
import com.daniel.marketplaceapp.testsupport.annotations.ServiceIntegrationTest
import com.daniel.marketplaceapp.testsupport.fixtures.randomUrl
import com.daniel.marketplaceapp.testsupport.steps.OrderSteps
import com.daniel.marketplaceapp.testsupport.steps.ProductSteps
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
    private lateinit var balanceTransactionRepository: BalanceTransactionRepository

    @Autowired
    private lateinit var orderSteps: OrderSteps

    @Autowired
    private lateinit var productSteps: ProductSteps

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
    fun `should create balance transactions for sellers on succeeded webhook`() {
        val sellerA = userSteps.createUser().id!!
        val sellerB = userSteps.createUser().id!!
        val product1 = productSteps.create(
            sellerId = sellerA,
            price = BigDecimal("100.00")
        )
        val product2 = productSteps.create(
            sellerId = sellerA,
            price = BigDecimal("150.00")
        )
        val product3 = productSteps.create(
            sellerId = sellerB,
            price = BigDecimal("300.00")
        )
        val order = orderSteps.createOrder(
            customerId = customerId,
            status = OrderStatus.PENDING_PAYMENT,
            items = mutableListOf(
                OrderItem(
                    orderId = null,
                    productId = product1.id!!,
                    sellerId = sellerA,
                    unitPrice = product1.price,
                    quantity = 2
                ),
                OrderItem(
                    orderId = null,
                    productId = product2.id!!,
                    sellerId = sellerA,
                    unitPrice = product2.price,
                    quantity = 1
                ),
                OrderItem(
                    orderId = null,
                    productId = product3.id!!,
                    sellerId = sellerB,
                    unitPrice = product3.price,
                    quantity = 3
                )
            ),
            totalAmount = product1.price * 2 + product2.price + product3.price * 3
        )
        val payment = createPayment(
            orderId = order.id!!,
            amount = order.totalAmount
        )

        paymentWebhookService.handleYooKassaWebhook(
            createWebhookRequest(
                externalId = payment.externalId!!,
                event = PAYMENT_SUCCEEDED_EVENT,
                status = YooKassaPaymentStatus.SUCCEEDED
            )
        )

        val transactions = balanceTransactionRepository.findAllByPaymentIdAndType(
            payment.id!!,
            BalanceTransactionType.CREDIT
        )
        val transactionsBySellerId = transactions.associateBy { it.userId }

        transactions.size shouldBe 2
        transactionsBySellerId[sellerA].shouldNotBeNull().amount shouldBe product1.price * 2 + product2.price
        transactionsBySellerId[sellerB].shouldNotBeNull().amount shouldBe product3.price * 3
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
        amount: Money = Money(BigDecimal("100.00")),
    ) = paymentRepository.save(
        Payment(
            id = null,
            orderId = orderId,
            externalId = externalId,
            confirmationUrl = randomUrl() + "/$externalId",
            amount = amount,
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
