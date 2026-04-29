package com.daniel.marketplaceapp.user.util

import com.daniel.marketplaceapp.user.dto.SearchResponse
import com.daniel.marketplaceapp.user.dto.UserResponse
import com.daniel.marketplaceapp.user.entity.User

fun User.toSearchResponse() = SearchResponse(username)

fun User.toResponse() = UserResponse(username, password)
