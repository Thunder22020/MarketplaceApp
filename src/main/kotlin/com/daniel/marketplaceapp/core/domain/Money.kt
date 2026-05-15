package com.daniel.marketplaceapp.core.domain

import jakarta.persistence.Embeddable
import java.math.BigDecimal

@Embeddable
data class Money(
    var amount: BigDecimal = BigDecimal.ZERO,
) {
    operator fun plus(money: Money) = Money(amount + money.amount)
    operator fun minus(money: Money) = Money(amount - money.amount)
    operator fun times(quantity: Int) = Money(amount * BigDecimal(quantity))

    companion object {
        val ZERO = Money(BigDecimal.ZERO.setScale(2))
    }
}
