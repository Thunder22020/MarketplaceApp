package com.daniel.marketplaceapp.product.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class EmptyUpdateProductRequestException(msg: String) :
    BaseApiException(msg, HttpStatus.BAD_REQUEST, "PRODUCT_UPDATE_REQUEST_EMPTY")
