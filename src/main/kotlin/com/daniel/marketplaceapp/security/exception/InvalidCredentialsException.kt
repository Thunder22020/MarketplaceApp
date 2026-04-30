package com.daniel.marketplaceapp.security.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class InvalidCredentialsException :
    BaseApiException(
        "Invalid username or password",
        HttpStatus.UNAUTHORIZED,
        "INVALID_CREDENTIALS"
    )
