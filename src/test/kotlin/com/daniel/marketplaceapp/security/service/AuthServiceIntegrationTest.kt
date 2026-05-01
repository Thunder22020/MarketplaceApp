package com.daniel.marketplaceapp.security.service

import com.daniel.marketplaceapp.security.exception.InvalidCredentialsException
import com.daniel.marketplaceapp.testsupport.fixtures.randomPassword
import com.daniel.marketplaceapp.testsupport.fixtures.randomUsername
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import com.daniel.marketplaceapp.user.dto.LoginRequest
import com.daniel.marketplaceapp.user.exception.UserNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AuthServiceIntegrationTest {
    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var userSteps: UserSteps

    @Test
    fun `should return token for existing user with valid credentials`() {
        val (username, password) = userSteps.createRandomUser()

        val request = LoginRequest(username, password)

        val token = authService.verifyAndGetToken(request)
        val claims = jwtService.parseToken(token)

        claims.shouldNotBeNull()
        claims.subject shouldBe username
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
        val (username, correctPassword) = userSteps.createRandomUser()
        val wrongPassword = "${correctPassword}_wrong"

        val request = LoginRequest(username, wrongPassword)

        shouldThrow<InvalidCredentialsException> {
            authService.verifyAndGetToken(request)
        }
    }
}
