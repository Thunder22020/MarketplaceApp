package com.daniel.marketplaceapp.payment.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class PaymentCreationFailedException(msg: String, )
    : BaseApiException(msg, HttpStatus.BAD_GATEWAY, "PAYMENT_CREATION_FAILED")
