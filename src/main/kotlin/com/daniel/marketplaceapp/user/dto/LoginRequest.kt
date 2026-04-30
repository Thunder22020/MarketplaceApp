package com.daniel.marketplaceapp.user.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank("Username must not be blank")
    val username: String,

    @field:NotBlank("Password must not be blank")
    val password: String,
)
