package com.daniel.marketplaceapp.order.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class OrderNotFoundException(message: String) :
    BaseApiException(message, HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND")
