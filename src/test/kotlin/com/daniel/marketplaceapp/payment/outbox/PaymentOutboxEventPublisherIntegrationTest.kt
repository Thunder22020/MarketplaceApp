package com.daniel.marketplaceapp.payment.outbox

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.core.event.payment.OrderItemPayload
import com.daniel.marketplaceapp.core.event.payment.PaymentSucceededEvent
import com.daniel.marketplaceapp.testsupport.kafka.KafkaIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.kafka.test.utils.KafkaTestUtils

@SpringBootTest(webEnvironment = NONE)
class PaymentOutboxEventPublisherIntegrationTest: KafkaIntegrationTest() {
    @Autowired
    private lateinit var publisher: PaymentOutboxEventPublisher

    @Autowired
    private lateinit var paymentOutboxEventRepository: PaymentOutboxEventRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should publish ready outbox event to kafka`() {
        val paymentId = UUID.randomUUID()
        val event = PaymentSucceededEvent(
            orderId = UUID.randomUUID(),
            paymentId = paymentId,
            orderItems = listOf(
                OrderItemPayload(
                    sellerId = UUID.randomUUID(),
                    unitPrice = Money(BigDecimal("150.00")),
                    quantity = 2
                )
            )
        )
        paymentOutboxEventRepository.save(
            PaymentOutboxEvent(
                id = null,
                aggregateId = paymentId,
                eventType = PAYMENT_SUCCEEDED_TOPIC,
                payload = objectMapper.writeValueAsString(event),
                status = PaymentOutboxEventStatus.NEW,
                attempts = 0,
                lastError = null,
                createdAt = Instant.now(),
                publishedAt = null
            )
        )

        val consumer = createStringConsumer("payment-outbox-publisher-test")
        consumer.subscribe(listOf(PAYMENT_SUCCEEDED_TOPIC))

        publisher.publish()

        val record = KafkaTestUtils.getSingleRecord(
            consumer,
            PAYMENT_SUCCEEDED_TOPIC,
            Duration.ofSeconds(3)
        )
        val publishedEvent = objectMapper.readValue(record.value(), PaymentSucceededEvent::class.java)
        val updatedOutboxEvent = paymentOutboxEventRepository.findByAggregateIdAndEventType(
            paymentId,
            PAYMENT_SUCCEEDED_TOPIC
        ).shouldNotBeNull()

        publishedEvent shouldBe event
        updatedOutboxEvent.status shouldBe PaymentOutboxEventStatus.PUBLISHED
        updatedOutboxEvent.publishedAt.shouldNotBeNull()
        updatedOutboxEvent.lastError shouldBe null
    }

    companion object {
        private const val PAYMENT_SUCCEEDED_TOPIC = "payment.succeeded"
    }
}
