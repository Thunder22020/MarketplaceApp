package com.daniel.marketplaceapp.security.service

import com.daniel.marketplaceapp.security.exception.InvalidCredentialsException
import com.daniel.marketplaceapp.testsupport.annotations.ServiceIntegrationTest
import com.daniel.marketplaceapp.testsupport.fixtures.TestUser
import com.daniel.marketplaceapp.testsupport.fixtures.randomPassword
import com.daniel.marketplaceapp.testsupport.fixtures.randomUsername
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import com.daniel.marketplaceapp.user.dto.request.LoginRequest
import com.daniel.marketplaceapp.user.exception.UserNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ServiceIntegrationTest
class AuthServiceIntegrationTest {
    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var userSteps: UserSteps

    private lateinit var registeredUser: TestUser

    @BeforeAll
    fun setUp() {
        registeredUser = userSteps.createRandomUser()
    }

    @Test
    fun `should return token for existing user with valid credentials`() {
        val request = LoginRequest(
            registeredUser.username,
            registeredUser.password
        )

        val token = authService.verifyAndGetToken(request)
        val claims = jwtService.parseToken(token)

        claims.shouldNotBeNull()
        claims.subject shouldBe registeredUser.username
    }

    @Test
    fun `should throw UserNotFoundException for non-existing user`() {
        val request = LoginRequest(randomUsername(), randomPassword())

        shouldThrow<UserNotFoundException> {
            authService.verifyAndGetToken(request)
        }
    }

    @Test
    fun `should throw InvalidCredentialsException when password is wrong`() {
        val wrongPassword = randomPassword()

        val request = LoginRequest(registeredUser.username, wrongPassword)

        shouldThrow<InvalidCredentialsException> {
            authService.verifyAndGetToken(request)
        }
    }
}
