package com.daniel.marketplaceapp.payment.exception

import com.daniel.marketplaceapp.core.exception.BaseApiException
import org.springframework.http.HttpStatus

class PaymentNotFoundException(message: String) :
    BaseApiException(message, HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND")
