package com.daniel.marketplaceapp.yookassa.payment

import com.fasterxml.jackson.annotation.JsonProperty

enum class YooKassaPaymentStatus {
    @JsonProperty("pending")
    PENDING,

    @JsonProperty("waiting_for_capture")
    WAITING_FOR_CAPTURE,

    @JsonProperty("succeeded")
    SUCCEEDED,

    @JsonProperty("canceled")
    CANCELED
}
