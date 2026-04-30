package com.daniel.marketplaceapp.user.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class UserAlreadyExistsException(message: String) :
    BaseApiException(message, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS")
