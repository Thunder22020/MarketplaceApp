package com.daniel.marketplaceapp.user.controller

import com.daniel.marketplaceapp.user.dto.response.SearchResponse
import com.daniel.marketplaceapp.user.mapper.toSearchResponse
import com.daniel.marketplaceapp.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID): ResponseEntity<SearchResponse> {
        val user = userService.getByIdOrThrow(id)
        return ResponseEntity.ok(user.toSearchResponse())
    }
}
