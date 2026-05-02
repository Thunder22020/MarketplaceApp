package com.daniel.marketplaceapp.product.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class ProductNotFoundException(message: String) :
    BaseApiException(message, HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND")
