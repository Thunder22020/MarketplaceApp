package com.daniel.marketplaceapp.security.controller

import com.daniel.marketplaceapp.testsupport.annotations.ControllerIntegrationTest
import com.daniel.marketplaceapp.testsupport.fixtures.TOO_SHORT_VALUE
import com.daniel.marketplaceapp.testsupport.fixtures.TestUser
import com.daniel.marketplaceapp.testsupport.fixtures.randomPassword
import com.daniel.marketplaceapp.testsupport.fixtures.randomString
import com.daniel.marketplaceapp.testsupport.fixtures.randomUsername
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import com.daniel.marketplaceapp.user.dto.request.LoginRequest
import com.daniel.marketplaceapp.user.dto.request.RegisterRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerIntegrationTest
class AuthControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userSteps: UserSteps

    private lateinit var registeredUser: TestUser

    @BeforeAll
    fun setUp() {
        registeredUser = userSteps.createRandomUser()
    }

    @Test
    fun `should register new user`() {
        val request = RegisterRequest(randomUsername(), randomPassword())

        mockMvc.perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.username").value(request.username))
    }

    @Test
    fun `should return conflict when register the existing user`() {
        val request = RegisterRequest(registeredUser.username, registeredUser.password)

        mockMvc.perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"))
    }

    @Test
    fun `should return bad request when register username is invalid`() {
        val request = RegisterRequest(TOO_SHORT_VALUE, randomPassword())

        mockMvc.perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0].field").value("username"))
            .andExpect(jsonPath("$.details[0].message")
                .value("Username must be between 3 and 30"))
    }

    @Test
    fun `should return bad request when register password is invalid`() {
        val request = RegisterRequest(randomUsername(), TOO_SHORT_VALUE)

        mockMvc.perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0].field").value("password"))
            .andExpect(jsonPath("$.details[0].message")
                .value("Password must be at least 6 characters long"))
    }

    @Test
    fun `should login existing user`() {
        val loginRequest = LoginRequest(registeredUser.username, registeredUser.password)

        mockMvc.perform(
            post(LOGIN_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
    }

    @Test
    fun `should return not found when login the non-existing user`() {
        val loginRequest = LoginRequest(randomUsername(), randomPassword())

        mockMvc.perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
            .andExpect(jsonPath("$.accessToken").doesNotExist())
    }

    @Test
    fun `should return unauthorized when login with wrong password`() {
        val wrongPassword = randomPassword()
        val loginRequest = LoginRequest(registeredUser.username, wrongPassword)

        mockMvc.perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
            .andExpect(jsonPath("$.accessToken").doesNotExist())
    }

    @Test
    fun `should return too many requests when login rate limit exceeded`() {
        val ip = "test-login-${randomString()}"
        val loginRequest = LoginRequest(randomUsername(), randomPassword())
        val requestBody = objectMapper.writeValueAsString(loginRequest)

        repeat(LOGIN_RATE_LIMIT) {
            mockMvc.perform(
                post(LOGIN_URL)
                    .header(FORWARDED_FOR_HEADER, ip)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
                .andExpect(status().isNotFound)
        }

        mockMvc.perform(
            post(LOGIN_URL)
                .header(FORWARDED_FOR_HEADER, ip)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isTooManyRequests)
            .andExpect(header().exists("Retry-After"))
            .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"))
    }

    @Test
    fun `should return too many requests when register rate limit exceeded`() {
        val ip = "test-register-${randomString()}"

        repeat(REGISTER_RATE_LIMIT) {
            val request = RegisterRequest(randomUsername(), randomPassword())

            mockMvc.perform(
                post(REGISTER_URL)
                    .header(FORWARDED_FOR_HEADER, ip)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
        }

        val limitedRequest = RegisterRequest(randomUsername(), randomPassword())
        mockMvc.perform(
            post(REGISTER_URL)
                .header(FORWARDED_FOR_HEADER, ip)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(limitedRequest))
        )
            .andExpect(status().isTooManyRequests)
            .andExpect(header().exists("Retry-After"))
            .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"))
    }

    companion object {
        private const val REGISTER_URL = "/api/auth/register"
        private const val LOGIN_URL = "/api/auth/login"
        private const val FORWARDED_FOR_HEADER = "X-Forwarded-For"
        private const val LOGIN_RATE_LIMIT = 10
        private const val REGISTER_RATE_LIMIT = 5
    }
}
