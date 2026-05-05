package com.daniel.marketplaceapp.testsupport.steps

import com.daniel.marketplaceapp.testsupport.fixtures.TestUser
import com.daniel.marketplaceapp.testsupport.fixtures.randomPassword
import com.daniel.marketplaceapp.testsupport.fixtures.randomUsername
import com.daniel.marketplaceapp.user.dto.request.RegisterRequest
import com.daniel.marketplaceapp.user.service.UserService
import org.springframework.stereotype.Component

@Component
class UserSteps(
    private val userService: UserService
) {
    fun createUser(
        username: String = randomUsername(),
        password: String = randomPassword()
    ) = userService.create(
        RegisterRequest(username, password)
    )

    fun createRandomUser(): TestUser {
        val username = randomUsername()
        val password = randomPassword()
        createUser(username, password)
        return TestUser(username, password)
    }
}
