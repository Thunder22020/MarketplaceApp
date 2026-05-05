package com.daniel.marketplaceapp.product.service

import com.daniel.marketplaceapp.product.dto.CreateProductRequest
import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.repository.ProductRepository
import com.daniel.marketplaceapp.testsupport.fixtures.randomString
import com.daniel.marketplaceapp.testsupport.steps.ProductSteps
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import com.daniel.marketplaceapp.user.entity.User
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrNull
import kotlin.test.Test

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductServiceIntegrationTest {
    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var userSteps: UserSteps

    @Autowired
    private lateinit var productSteps: ProductSteps

    private lateinit var userA: User
    private lateinit var userB: User

    @BeforeAll
    fun setUp() {
        userA = userSteps.createUser(username = "userA")
        userB = userSteps.createUser(username = "userB")
    }

    @Test
    fun `should create a new product`() {
        val sellerId = requireNotNull(userA.id)
        val req = CreateProductRequest(
            title = randomString(),
            description = randomString(),
            price = BigDecimal("100.00")
        )
        val result = productService.create(req, sellerId)
        result.id.shouldNotBeNull()

        val product = productRepository.findById(result.id!!).getOrNull()
        product.shouldNotBeNull()

        product.id.shouldNotBeNull()
        product.id shouldBe result.id
        product.title shouldBe req.title
        product.description shouldBe req.description
        product.price.amount shouldBe req.price
        product.status shouldBe ProductStatus.ACTIVE
        product.seller.id shouldBe sellerId
        product.updatedAt.shouldBeNull()
    }

    @Test
    fun `should soft delete a product`() {
        val sellerId = requireNotNull(userA.id)
        val productBeforeDelete = productSteps.createProduct(
            sellerId = sellerId,
            title = randomString(),
            description = randomString(),
            price = BigDecimal("100.00")
        )
        val productId = requireNotNull(productBeforeDelete.id)
        productService.delete(productId, sellerId)
        val productAfterDelete = productRepository.findByIdOrNull(productId).shouldNotBeNull()

        productAfterDelete.updatedAt.shouldNotBeNull()
        productAfterDelete.status shouldBe ProductStatus.DELETED
    }

    @Test
    fun `should filter products for not seller`() {
        //
    }
}
