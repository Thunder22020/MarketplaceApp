package com.daniel.marketplaceapp.user.mapper

import com.daniel.marketplaceapp.user.domain.User
import com.daniel.marketplaceapp.user.dto.response.SearchResponse
import com.daniel.marketplaceapp.user.dto.response.UserResponse

fun User.toSearchResponse() = SearchResponse(username)

fun User.toResponse() = UserResponse(username)
