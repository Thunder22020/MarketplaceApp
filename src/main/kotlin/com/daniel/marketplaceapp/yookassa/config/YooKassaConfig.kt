package com.daniel.marketplaceapp.yookassa.config

import com.daniel.marketplaceapp.yookassa.error.YooKassaApiException
import com.daniel.marketplaceapp.yookassa.error.YooKassaErrorResponse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.jackson.jackson
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class YooKassaConfig(
    @Value("\${app.payment.shop.secret-key}")
    private val secretKey: String,
    @Value("\${app.payment.shop.id}")
    private val shopId: String,
) {
    @Bean
    fun httpClient(): HttpClient {
        val objectMapper = yookassaObjectMapper()
        return HttpClient(CIO) {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(
                            username = shopId,
                            password = secretKey
                        )
                    }
                }
            }
            install(ContentNegotiation) {
                jackson {
                    configureYooKassaObjectMapper(this)
                }
            }
            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, _ ->
                    val responseException = cause as? ResponseException
                        ?: return@handleResponseExceptionWithRequest
                    val response = responseException.response
                    val rawBody = response.bodyAsText()
                    val apiError = runCatching {
                        objectMapper.readValue<YooKassaErrorResponse>(rawBody)
                    }.getOrNull()

                    throw YooKassaApiException(
                        statusCode = response.status.value,
                        error = apiError,
                        rawBody = rawBody,
                        cause = responseException
                    )
                }
            }
            expectSuccess = true
        }
    }

    private fun yookassaObjectMapper(): ObjectMapper =
        configureYooKassaObjectMapper(jacksonObjectMapper())

    private fun configureYooKassaObjectMapper(objectMapper: ObjectMapper): ObjectMapper =
        objectMapper
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}
