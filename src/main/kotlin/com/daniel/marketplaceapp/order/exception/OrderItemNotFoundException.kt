package com.daniel.marketplaceapp.order.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class OrderItemNotFoundException(message: String) :
    BaseApiException(message, HttpStatus.NOT_FOUND, "ORDER_ITEM_NOT_FOUND")
