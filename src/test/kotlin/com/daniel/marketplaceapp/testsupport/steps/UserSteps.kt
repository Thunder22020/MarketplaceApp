package com.daniel.marketplaceapp.testsupport.steps

import com.daniel.marketplaceapp.testsupport.data.TestUser
import com.daniel.marketplaceapp.testsupport.data.randomPassword
import com.daniel.marketplaceapp.testsupport.data.randomUsername
import com.daniel.marketplaceapp.user.dto.RegisterRequest
import com.daniel.marketplaceapp.user.service.UserService
import org.springframework.stereotype.Component

@Component
class UserSteps(
    private val userService: UserService
) {
    fun createUser(
        username: String = randomUsername(),
        password: String = randomPassword()
    ) = userService.save(
        RegisterRequest(username, password)
    )

    fun createRandomUser(): TestUser {
        val username = randomUsername()
        val password = randomPassword()
        createUser(username, password)
        return TestUser(username, password)
    }
}
