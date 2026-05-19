package com.daniel.marketplaceapp.user.repository

import com.daniel.marketplaceapp.jooq.Tables.USERS
import com.daniel.marketplaceapp.user.domain.User
import java.util.UUID
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository

@Repository
class JooqUserRepository(
    private val dsl: DSLContext
) : UserRepository {
    override fun save(user: User): User {
        val id = UUID.randomUUID()
        dsl.insertInto(USERS)
            .set(USERS.ID, id)
            .set(USERS.USERNAME, user.username)
            .set(USERS.PASSWORD_HASH, user.passwordHash)
            .execute()
        user.id = id
        return user
    }

    override fun findById(id: UUID) =
        dsl.selectFrom(USERS)
            .where(USERS.ID.eq(id))
            .fetchOne()
            ?.toDomain()

    override fun findByUsername(username: String) =
        dsl.selectFrom(USERS)
            .where(USERS.USERNAME.eq(username))
            .fetchOne()
            ?.toDomain()

    private fun Record.toDomain(): User = User(
        id = requireNotNull(get(USERS.ID)),
        username = requireNotNull(get(USERS.USERNAME)),
        passwordHash = requireNotNull(get(USERS.PASSWORD_HASH))
    )
}
