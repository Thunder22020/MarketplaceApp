package com.daniel.marketplaceapp.user.repository

import com.daniel.marketplaceapp.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?
}
