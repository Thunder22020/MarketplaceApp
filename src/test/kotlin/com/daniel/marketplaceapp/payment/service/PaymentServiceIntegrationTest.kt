package com.daniel.marketplaceapp.payment.service

import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.payment.dto.PaymentProviderResponse
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import com.daniel.marketplaceapp.payment.exception.PaymentCannotBeCreatedException
import com.daniel.marketplaceapp.payment.exception.PaymentCreationFailedException
import com.daniel.marketplaceapp.payment.provider.PaymentProviderClient
import com.daniel.marketplaceapp.payment.repository.PaymentRepository
import com.daniel.marketplaceapp.testsupport.annotations.ServiceIntegrationTest
import com.daniel.marketplaceapp.testsupport.fixtures.randomUrl
import com.daniel.marketplaceapp.testsupport.steps.OrderSteps
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.UUID
import org.junit.jupiter.api.BeforeAll
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.test.Test

@ServiceIntegrationTest
class PaymentServiceIntegrationTest {
    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var orderSteps: OrderSteps

    @Autowired
    private lateinit var userSteps: UserSteps

    @MockitoBean
    private lateinit var paymentProviderClient: PaymentProviderClient

    private lateinit var customerId: UUID

    @BeforeAll
    fun setUp() {
        customerId = userSteps.createUser().id!!
    }

    @Test
    fun `should create payment for order`() {
        val externalId = UUID.randomUUID().toString()
        val confirmationUrl = randomUrl()
        mockPaymentProviderClient(externalId, confirmationUrl)

        val order = orderSteps.createOrder(
            customerId = customerId,
            status = OrderStatus.PENDING_PAYMENT
        )

        val result = paymentService.createPaymentForOrder(order)
        val payment = paymentRepository.findById(result.id!!)
            .shouldNotBeNull()

        payment.id.shouldNotBeNull()
        payment.updatedAt.shouldNotBeNull()
        payment.orderId shouldBe order.id
        payment.status shouldBe PaymentStatus.PENDING
        payment.externalId shouldBe externalId
        payment.confirmationUrl shouldBe confirmationUrl
        payment.amount shouldBe order.totalAmount
    }

    @Test
    fun `should handle exception and save FAILED payment`() {
        mockPaymentProviderClientWithException()

        val order = orderSteps.createOrder(
            customerId = customerId,
            status = OrderStatus.PENDING_PAYMENT
        )

        shouldThrow<PaymentCreationFailedException> {
            paymentService.createPaymentForOrder(order)
        }
        val payment = paymentRepository.findByOrderId(order.id!!)
            .shouldNotBeNull()

        payment.externalId.shouldBeNull()
        payment.confirmationUrl.shouldBeNull()
        payment.id.shouldNotBeNull()
        payment.updatedAt.shouldNotBeNull()
        payment.orderId shouldBe order.id
        payment.status shouldBe PaymentStatus.FAILED
        payment.amount shouldBe order.totalAmount
    }

    @Test
    fun `should throw when order status is not PENDING_PAYMENT`() {
        val order = orderSteps.createOrder(
            customerId = customerId,
            status = OrderStatus.DRAFT
        )

        shouldThrow<PaymentCannotBeCreatedException>{
            paymentService.createPaymentForOrder(order)
        }
    }

    private fun mockPaymentProviderClient(externalId: String, confirmationUrl: String) {
        whenever(paymentProviderClient.createPayment(any()))
            .thenReturn(
                PaymentProviderResponse(
                    externalId = externalId,
                    confirmationUrl = confirmationUrl
                )
            )
    }

    private fun mockPaymentProviderClientWithException() {
        whenever(paymentProviderClient.createPayment(any()))
            .thenThrow(RuntimeException("Something went wrong"))
    }
}
