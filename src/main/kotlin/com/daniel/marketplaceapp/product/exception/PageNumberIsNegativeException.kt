package com.daniel.marketplaceapp.product.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class PageNumberIsNegativeException(msg: String)
    : BaseApiException(msg, HttpStatus.BAD_REQUEST, "PAGE_NUMBER_IS_NEGATIVE")
