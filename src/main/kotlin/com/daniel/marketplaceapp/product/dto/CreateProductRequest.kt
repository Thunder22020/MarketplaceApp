package com.daniel.marketplaceapp.product.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreateProductRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 255)
    var title: String,

    var description: String? = null,

    @field:Positive
    var price: BigDecimal,
)
