package com.daniel.marketplaceapp.testsupport.fixtures

import java.util.UUID

fun randomString(): String = UUID.randomUUID().toString().take(8)

fun randomUsername(): String = "user_${randomString()}"

fun randomPassword(): String = "password_${randomString()}"

fun randomUrl(): String = "https://${randomString()}"

const val TOO_SHORT_VALUE = "."
