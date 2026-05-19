package com.daniel.marketplaceapp.user.repository

import com.daniel.marketplaceapp.user.domain.User
import java.util.UUID

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UUID): User?
    fun findByUsername(username: String): User?
}
