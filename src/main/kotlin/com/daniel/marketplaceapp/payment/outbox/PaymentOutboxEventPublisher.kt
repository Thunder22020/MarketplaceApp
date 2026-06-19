package com.daniel.marketplaceapp.payment.outbox

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PaymentOutboxEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val paymentOutboxEventRepository: PaymentOutboxEventRepository,
) {
    private val log = LoggerFactory.getLogger(PaymentOutboxEventPublisher::class.java)

    @Scheduled(fixedDelay = 3000)
    fun publish() {
        val events = paymentOutboxEventRepository.findReadyForPublish(50, 10)

        val futuresByEvents = events.associateWith { event ->
            kafkaTemplate.send(event.eventType, event.payload)
        }

        futuresByEvents.forEach { (event, future) ->
            try {
                future.join()
                event.markPublished()
            } catch (ex: Exception) {
                log.error("Failed to publish outbox event. eventId={}", event.id, ex)
                event.markFailed(ex.message ?: "Unknown error")
            }
        }

        paymentOutboxEventRepository.saveAll(events)
    }
}
