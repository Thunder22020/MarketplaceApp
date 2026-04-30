package com.daniel.marketplaceapp.user.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class UserNotFoundException(message: String) :
    BaseApiException(message, HttpStatus.NOT_FOUND, "USER_NOT_FOUND")
