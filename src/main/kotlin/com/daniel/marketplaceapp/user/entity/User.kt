package com.daniel.marketplaceapp.user.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(unique = true, nullable = false)
    var username: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,
)
