package com.daniel.marketplaceapp.user.service

import com.daniel.marketplaceapp.user.dto.RegisterRequest
import com.daniel.marketplaceapp.user.entity.User
import com.daniel.marketplaceapp.user.exception.UserAlreadyExistsException
import com.daniel.marketplaceapp.user.exception.UserNotFoundException
import com.daniel.marketplaceapp.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder
) {
    fun save(dto: RegisterRequest): User {
        if (userRepository.findByUsername(dto.username) != null) {
            throw UserAlreadyExistsException("User already exists")
        }

        val entity = User(
            username = dto.username,
            password = requireNotNull(encoder.encode(dto.password))
        )

        return userRepository.save(entity)
    }

    fun findByIdOrThrow(id: UUID): User =
        userRepository.findById(id).orElseThrow {
            UserNotFoundException("User with ID:$id not found")
        }
}
