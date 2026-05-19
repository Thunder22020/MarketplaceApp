package com.daniel.marketplaceapp.core.config

import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JooqConfig {
    @Bean
    fun jooqSettings(): Settings = Settings()
        .withRenderQuotedNames(RenderQuotedNames.NEVER)
        .withRenderNameCase(RenderNameCase.LOWER)
}
