package com.daniel.marketplaceapp.testsupport.kafka

import java.util.UUID
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.ConfluentKafkaContainer

@Testcontainers
abstract class KafkaIntegrationTest {
    protected fun kafkaBootstrapServers(): String =
        kafka.bootstrapServers

    protected fun createStringConsumer(groupIdPrefix: String) =
        DefaultKafkaConsumerFactory(
            KafkaTestUtils.consumerProps(
                kafkaBootstrapServers(),
                "$groupIdPrefix-${UUID.randomUUID()}",
                "true"
            ).also {
                it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            },
            StringDeserializer(),
            StringDeserializer()
        ).createConsumer()

    companion object {
        @Container
        @JvmStatic
        val kafka = ConfluentKafkaContainer("confluentinc/cp-kafka:8.1.0")

        @JvmStatic
        @DynamicPropertySource
        fun kafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
        }
    }
}
