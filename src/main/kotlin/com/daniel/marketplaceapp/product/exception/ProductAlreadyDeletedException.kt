package com.daniel.marketplaceapp.product.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class ProductAlreadyDeletedException(msg: String) :
    BaseApiException(msg, HttpStatus.CONFLICT, "PRODUCT_ALREADY_DELETED")
