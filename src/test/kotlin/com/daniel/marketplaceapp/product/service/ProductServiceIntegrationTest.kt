package com.daniel.marketplaceapp.product.service

import com.daniel.marketplaceapp.product.dto.CreateProductRequest
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.exception.EmptyUpdateProductRequestException
import com.daniel.marketplaceapp.product.exception.ProductAlreadyDeletedException
import com.daniel.marketplaceapp.product.exception.ProductNotFoundException
import com.daniel.marketplaceapp.product.repository.ProductRepository
import com.daniel.marketplaceapp.testsupport.annotations.ServiceIntegrationTest
import com.daniel.marketplaceapp.testsupport.fixtures.randomString
import com.daniel.marketplaceapp.testsupport.steps.ProductSteps
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import com.daniel.marketplaceapp.user.domain.User
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

@ServiceIntegrationTest
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
        userA = userSteps.createUser()
        userB = userSteps.createUser()
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

        val product = productRepository.findById(requireNotNull(result.id))
            .shouldNotBeNull()

        product.id.shouldNotBeNull()
        product.id shouldBe result.id
        product.title shouldBe req.title
        product.description shouldBe req.description
        product.price.amount shouldBe req.price
        product.status shouldBe ProductStatus.ACTIVE
        product.sellerId shouldBe sellerId
        product.updatedAt.shouldBeNull()
    }

    @Test
    fun `should soft delete a product`() {
        val sellerId = requireNotNull(userA.id)
        val productBeforeDelete = productSteps.create(
            sellerId = sellerId,
            title = randomString(),
            description = randomString(),
            price = BigDecimal("100.00")
        )
        val productId = requireNotNull(productBeforeDelete.id)
        productService.delete(productId, sellerId)
        val productAfterDelete = productRepository.findById(productId).shouldNotBeNull()

        productAfterDelete.updatedAt.shouldNotBeNull()
        productAfterDelete.status shouldBe ProductStatus.DELETED
    }

    @Test
    fun `should show only active products on find all`() {
        val sellerId = requireNotNull(userA.id)

        val deletedProduct = productSteps.create(sellerId = sellerId)
        val hiddenProduct = productSteps.create(sellerId = sellerId)
        val activeProduct = productSteps.create(sellerId = sellerId)

        productService.delete(requireNotNull(deletedProduct.id), sellerId)
        productService.hide(requireNotNull(hiddenProduct.id), sellerId)

        val products = productService.getAll()

        products.forAll {
            it.status shouldBe ProductStatus.ACTIVE
        }

        val ids = products.map { it.id }
        ids.shouldContain(activeProduct.id)
        ids.shouldNotContainAll(listOf(deletedProduct.id, hiddenProduct.id))
    }

    @Test
    fun `should filter products for non seller when find all by seller`() {
        val customerId = requireNotNull(userA.id)
        val sellerId = requireNotNull(userB.id)

        val product1 = productSteps.create(sellerId = sellerId)
        val product2 = productSteps.create(sellerId = sellerId)
        val product3 = productSteps.create(sellerId = sellerId)

        productService.delete(requireNotNull(product1.id), sellerId)
        productService.hide(requireNotNull(product2.id), sellerId)

        val foundProducts = productService.getAllBySellerId(sellerId, customerId)
        foundProducts.shouldHaveSize(1)

        val onlyProductFound = foundProducts[0]
        onlyProductFound.id shouldBe product3.id
        onlyProductFound.title shouldBe product3.title
        onlyProductFound.sellerId shouldBe sellerId
    }

    @Test
    fun `should filter deleted products for seller when find all by seller`() {
        val sellerId = requireNotNull(userA.id)

        val deletedProduct = productSteps.create(sellerId = sellerId)
        val hiddenProduct = productSteps.create(sellerId = sellerId)
        val activeProduct = productSteps.create(sellerId = sellerId)

        productService.delete(requireNotNull(deletedProduct.id), sellerId)
        productService.hide(requireNotNull(hiddenProduct.id), sellerId)

        val foundProducts = productService.getAllBySellerId(sellerId, sellerId)
        foundProducts.shouldHaveSize(2)

        val foundHiddenProduct = foundProducts.first { it.id == hiddenProduct.id }
        val foundActiveProduct = foundProducts.first { it.id == activeProduct.id }

        foundHiddenProduct.status shouldBe ProductStatus.HIDDEN
        foundHiddenProduct.title shouldBe hiddenProduct.title
        foundActiveProduct.status shouldBe ProductStatus.ACTIVE
        foundActiveProduct.title shouldBe activeProduct.title
    }

    @Test
    fun `should hide product`() {
        val sellerId = requireNotNull(userA.id)
        val product = productSteps.create(sellerId = sellerId)

        productService.hide(requireNotNull(product.id), sellerId)

        val hiddenProduct = productRepository.findById(requireNotNull(product.id)).shouldNotBeNull()

        hiddenProduct.updatedAt.shouldNotBeNull()
        hiddenProduct.title shouldBe product.title
        hiddenProduct.status shouldBe ProductStatus.HIDDEN
    }

    @Test
    fun `should unhide product`() {
        val sellerId = requireNotNull(userA.id)
        val product = productSteps.create(sellerId = sellerId)
        productService.hide(requireNotNull(product.id), sellerId)
        productService.unhide(requireNotNull(product.id), sellerId)

        val unhiddenProduct = productRepository.findById(requireNotNull(product.id)).shouldNotBeNull()

        unhiddenProduct.updatedAt.shouldNotBeNull()
        unhiddenProduct.title shouldBe product.title
        unhiddenProduct.status shouldBe ProductStatus.ACTIVE
    }

    @Test
    fun `should update product`() {
        val sellerId = requireNotNull(userA.id)
        val productBeforeUpdate = productSteps.create(sellerId = sellerId)
        val productId = requireNotNull(productBeforeUpdate.id)

        val req = UpdateProductRequest(
            title = randomString(),
            description = randomString(),
            price = BigDecimal("150.00")
        )
        productService.update(req, productId, sellerId)

        val productAfterUpdate = productRepository.findById(productId).shouldNotBeNull()

        productAfterUpdate.id shouldBe productId
        productAfterUpdate.title shouldBe req.title
        productAfterUpdate.description shouldBe req.description
        productAfterUpdate.price.amount shouldBe BigDecimal("150.00")
    }

    @Test
    fun `should throw on empty update request`() {
        val sellerId = requireNotNull(userA.id)
        val product = productSteps.create(sellerId = sellerId)
        val productId = requireNotNull(product.id)

        val emptyUpdateReq = UpdateProductRequest()
        shouldThrow<EmptyUpdateProductRequestException>{
            productService.update(emptyUpdateReq, productId, sellerId)
        }
    }

    @Test
    fun `should throw on update for non seller`() {
        val sellerId = requireNotNull(userA.id)
        val nonSellerId = requireNotNull(userB.id)
        val product = productSteps.create(sellerId = sellerId)
        val productId = requireNotNull(product.id)

        val req = UpdateProductRequest(title = randomString())
        shouldThrow<ProductNotFoundException>{
            productService.update(req, productId, nonSellerId)
        }
    }

    @Test
    fun `should throw when hide deleted product`() {
        val sellerId = requireNotNull(userA.id)
        val product = productSteps.create(sellerId = sellerId)
        productService.delete(requireNotNull(product.id), sellerId)

        shouldThrow<ProductAlreadyDeletedException> {
            productService.hide(requireNotNull(product.id), sellerId)
        }
    }

    @Test
    fun `should throw when unhide deleted product`() {
        val sellerId = requireNotNull(userA.id)
        val product = productSteps.create(sellerId = sellerId)
        productService.delete(requireNotNull(product.id), sellerId)

        shouldThrow<ProductAlreadyDeletedException> {
            productService.unhide(requireNotNull(product.id), sellerId)
        }
    }

    @Test
    fun `should throw when update deleted product`() {
        val sellerId = requireNotNull(userA.id)
        val product = productSteps.create(sellerId = sellerId)
        productService.delete(requireNotNull(product.id), sellerId)

        shouldThrow<ProductAlreadyDeletedException> {
            productService.update(
                req = UpdateProductRequest(title = randomString()),
                productId = requireNotNull(product.id),
                sellerId = sellerId
            )
        }
    }

    @Test
    fun `should throw when hide or unhide by non seller`() {
        val sellerId = requireNotNull(userA.id)
        val nonSellerId = requireNotNull(userB.id)
        val product = productSteps.create(sellerId = sellerId)

        shouldThrow<ProductNotFoundException> {
            productService.hide(requireNotNull(product.id), nonSellerId)
        }
    }

    @Test
    fun `should throw when unhide by non seller`() {
        val sellerId = requireNotNull(userA.id)
        val nonSellerId = requireNotNull(userB.id)
        val product = productSteps.create(sellerId = sellerId)

        shouldThrow<ProductNotFoundException> {
            productService.unhide(requireNotNull(product.id), nonSellerId)
        }
    }
}
