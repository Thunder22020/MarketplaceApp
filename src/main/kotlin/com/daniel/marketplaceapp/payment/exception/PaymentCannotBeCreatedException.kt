package com.daniel.marketplaceapp.payment.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class PaymentCannotBeCreatedException(msg: String)
    : BaseApiException(msg, HttpStatus.CONFLICT, "PAYMENT_CANNOT_BE_CREATED")
