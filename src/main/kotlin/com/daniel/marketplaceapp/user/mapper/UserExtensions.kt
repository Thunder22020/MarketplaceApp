package com.daniel.marketplaceapp.user.mapper

import com.daniel.marketplaceapp.user.dto.response.SearchResponse
import com.daniel.marketplaceapp.user.dto.response.UserResponse
import com.daniel.marketplaceapp.user.entity.User

fun User.toSearchResponse() = SearchResponse(username)

fun User.toResponse() = UserResponse(username)
