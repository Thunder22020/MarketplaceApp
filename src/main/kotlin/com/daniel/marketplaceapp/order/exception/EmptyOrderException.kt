package com.daniel.marketplaceapp.order.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class EmptyOrderException(message: String) :
    BaseApiException(message, HttpStatus.BAD_REQUEST, "EMPTY_ORDER")
