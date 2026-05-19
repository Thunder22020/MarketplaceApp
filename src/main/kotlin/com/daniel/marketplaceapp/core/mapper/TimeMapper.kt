package com.daniel.marketplaceapp.core.mapper

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun Instant.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(this, ZoneOffset.UTC)

fun LocalDateTime.toInstantUtc(): Instant =
    this.toInstant(ZoneOffset.UTC)
