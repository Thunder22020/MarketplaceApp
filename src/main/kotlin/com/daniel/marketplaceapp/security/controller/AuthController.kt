package com.daniel.marketplaceapp.security.controller

import com.daniel.marketplaceapp.security.dto.AccessTokenResponse
import com.daniel.marketplaceapp.security.service.AuthService
import com.daniel.marketplaceapp.user.dto.RegisterRequest
import com.daniel.marketplaceapp.user.dto.UserRequest
import com.daniel.marketplaceapp.user.service.UserService
import com.daniel.marketplaceapp.user.util.toResponse
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
    fun register(@RequestBody user: RegisterRequest) =
        ResponseEntity.ok(userService.save(user).toResponse())

    @PostMapping("/login")
    fun login(@RequestBody user: UserRequest) =
        ResponseEntity.ok(AccessTokenResponse(authService.verifyAndGetToken(user)))
}
