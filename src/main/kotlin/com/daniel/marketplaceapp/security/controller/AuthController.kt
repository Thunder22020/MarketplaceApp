package com.daniel.marketplaceapp.security.controller

import com.daniel.marketplaceapp.security.dto.AccessTokenResponse
import com.daniel.marketplaceapp.security.service.AuthService
import com.daniel.marketplaceapp.user.dto.LoginRequest
import com.daniel.marketplaceapp.user.dto.RegisterRequest
import com.daniel.marketplaceapp.user.service.UserService
import com.daniel.marketplaceapp.user.util.toResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val authService: AuthService,
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody user: RegisterRequest) =
        ResponseEntity.ok(userService.save(user).toResponse())

    @PostMapping("/login")
    fun login(@Valid @RequestBody user: LoginRequest) =
        ResponseEntity.ok(AccessTokenResponse(authService.verifyAndGetToken(user)))
}
