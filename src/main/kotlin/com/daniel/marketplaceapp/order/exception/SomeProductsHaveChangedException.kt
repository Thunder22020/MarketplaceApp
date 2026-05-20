package com.daniel.marketplaceapp.order.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class SomeProductsHaveChangedException(message: String) :
    BaseApiException(message, HttpStatus.CONFLICT, "PRODUCTS_HAVE_CHANGED")
