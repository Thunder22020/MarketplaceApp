package com.daniel.marketplaceapp.security.service

import com.daniel.marketplaceapp.security.exception.InvalidCredentialsException
import com.daniel.marketplaceapp.user.dto.LoginRequest
import com.daniel.marketplaceapp.user.entity.User
import com.daniel.marketplaceapp.user.exception.UserNotFoundException
import com.daniel.marketplaceapp.user.repository.UserRepository
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthServiceTest {
    private val authManager: AuthenticationManager = mock()
    private val jwtService: JwtService = mock()
    private val userRepository: UserRepository = mock()

    private val authService = AuthService(authManager, jwtService, userRepository)

    private val user = User(id=1, "Danila", "123123")
    private val request = LoginRequest("Danila", "123123")

    private var result: String? = null

    @Test
    fun `should return token on valid login request`() {
        givenUserWasFound()
        givenTokenGenerated()

        whenLoginRequested()

        assertEquals("accessToken", result)
    }

    @Test
    fun `should throw exception on user not found`() {
        givenUserWasNotFound()

        whenLoginRequestedThrows<UserNotFoundException>()

        verify(jwtService, never()).generateToken(any())
    }

    @Test
    fun `should throw InvalidCredentialsException when password is wrong`() {
        givenUserWasFound()
        givenInvalidAuthentication()

        whenLoginRequestedThrows<InvalidCredentialsException>()

        verify(jwtService, never()).generateToken(any())
    }

    private fun givenUserWasFound() {
        whenever(userRepository.findByUsername(request.username))
            .thenReturn(user)
    }

    private fun givenUserWasNotFound() {
        whenever(userRepository.findByUsername(request.username))
            .thenReturn(null)
    }

    private fun givenTokenGenerated() {
        whenever(jwtService.generateToken(request.username))
            .thenReturn("accessToken")
    }

    private fun givenInvalidAuthentication() {
        whenever(authManager.authenticate(any<UsernamePasswordAuthenticationToken>()))
            .thenThrow(BadCredentialsException::class.java)
    }

    private fun whenLoginRequested() {
        result = authService.verifyAndGetToken(request)
    }

    private inline fun <reified T : Throwable> whenLoginRequestedThrows() {
        assertThrows<T> {
            authService.verifyAndGetToken(request)
        }
    }
}
