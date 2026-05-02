package com.daniel.marketplaceapp.user.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank("Username must not be blank")
    @field:Size(min = 3, max = 30, message = "Username must be between 3 and 30")
    val username: String,

    @field:NotBlank("Password must not be blank")
    @field:Size(min = 6, message = "Password must be at least 6 characters long")
    val password: String,
)
