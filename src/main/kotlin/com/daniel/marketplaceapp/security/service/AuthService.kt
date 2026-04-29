package com.daniel.marketplaceapp.security.service

import com.daniel.marketplaceapp.user.dto.UserRequest
import com.daniel.marketplaceapp.user.exception.UserNotFoundException
import com.daniel.marketplaceapp.user.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
) {
    fun verifyAndGetToken(user: UserRequest): String {
        if (userRepository.findByUsername(user.username) == null) {
            throw UserNotFoundException("User not found")
        }

        val authToken = UsernamePasswordAuthenticationToken(
            user.username,
            user.password
        )
        authManager.authenticate(authToken)

        val accessToken = jwtService.generateToken(user.username)

        return accessToken
    }
}
