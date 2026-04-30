package com.daniel.marketplaceapp.security.service

import com.daniel.marketplaceapp.security.exception.InvalidCredentialsException
import com.daniel.marketplaceapp.user.dto.LoginRequest
import com.daniel.marketplaceapp.user.exception.UserNotFoundException
import com.daniel.marketplaceapp.user.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
) {
    fun verifyAndGetToken(loginRequest: LoginRequest): String {
        if (userRepository.findByUsername(loginRequest.username) == null) {
            throw UserNotFoundException("User not found")
        }
        tryAuthenticate(loginRequest)
        val accessToken = jwtService.generateToken(loginRequest.username)
        return accessToken
    }

    fun tryAuthenticate(loginRequest: LoginRequest) {
        try {
            val authToken = UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
            authManager.authenticate(authToken)
        } catch (_: BadCredentialsException) {
            throw InvalidCredentialsException()
        }
    }
}
