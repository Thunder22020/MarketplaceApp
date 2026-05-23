package com.daniel.marketplaceapp.yookassa.payment

import com.fasterxml.jackson.annotation.JsonProperty

data class Recipient(
    @JsonProperty("account_id")
    val accountId: String,
    @JsonProperty("gateway_id")
    val gatewayId: String
)
