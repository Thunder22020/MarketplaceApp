package com.daniel.marketplaceapp.core.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {
    @Bean
    fun paymentSucceededTopic(): NewTopic =
        TopicBuilder.name("payment.succeeded")
            .partitions(3)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
            .config(TopicConfig.RETENTION_BYTES_CONFIG, "524288000")
            .build()
}
