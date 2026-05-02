package com.daniel.marketplaceapp.core.domain

import jakarta.persistence.Embeddable
import java.math.BigDecimal

@Embeddable
data class Money(
    var amount: BigDecimal = BigDecimal.ZERO,
)
