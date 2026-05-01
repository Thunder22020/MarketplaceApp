package com.daniel.marketplaceapp.testsupport.fixtures

import java.util.*

fun randomString(): String = UUID.randomUUID().toString().take(8)

fun randomUsername(): String = "user_${randomString()}"

fun randomPassword(): String = "password_${randomString()}"

const val TOO_SHORT_VALUE = "."
