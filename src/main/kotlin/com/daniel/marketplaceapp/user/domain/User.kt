package com.daniel.marketplaceapp.user.domain

import java.io.Serializable
import java.util.UUID

class User(
    var id: UUID?,
    val username: String,
    val passwordHash: String
) : Serializable
