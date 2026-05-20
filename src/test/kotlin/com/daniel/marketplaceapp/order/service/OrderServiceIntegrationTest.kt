package com.daniel.marketplaceapp.order.service

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.exception.EmptyOrderException
import com.daniel.marketplaceapp.order.exception.OrderItemNotFoundException
import com.daniel.marketplaceapp.order.exception.OrderNotFoundException
import com.daniel.marketplaceapp.order.exception.SomeProductsHaveChangedException
import com.daniel.marketplaceapp.order.repository.OrderRepository
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.service.ProductService
import com.daniel.marketplaceapp.testsupport.steps.ProductSteps
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal
import java.util.UUID
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@Transactional
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderServiceIntegrationTest {
    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var userSteps: UserSteps

    @Autowired
    private lateinit var productSteps: ProductSteps

    private lateinit var userId: UUID

    @BeforeAll
    fun setUp() {
        userId = userSteps.createUser().id!!
    }

    @Test
    fun `should checkout draft order`() {
        val product1 = productSteps.create(sellerId = userId)
        val product2 = productSteps.create(sellerId = userId)

        repeat(2) {
            orderService.addItemToCart(product1.id!!, userId)
        }
        orderService.addItemToCart(product2.id!!, userId)
        val totalAmount = orderRepository.findDraftByCustomerId(userId)
            .shouldNotBeNull().totalAmount

        val order = orderService.checkout(userId)

        order.updatedAt.shouldNotBeNull()
        order.totalAmount shouldBe totalAmount
        order.status shouldBe OrderStatus.PENDING_PAYMENT
    }

    @Test
    fun `should process product price change on checkout`() {
        val product1 = productSteps.create(sellerId = userId)
        val product2 = productSteps.create(sellerId = userId)

        repeat(2) {
            orderService.addItemToCart(product1.id!!, userId)
        }
        orderService.addItemToCart(product2.id!!, userId)
        val realAmountBeforeCheckout = product1.price * 2 + product2.price

        val totalAmountBeforeCheckout = orderRepository.findDraftByCustomerId(userId)
            .shouldNotBeNull().totalAmount
        totalAmountBeforeCheckout shouldBe realAmountBeforeCheckout

        val newUnitPrice = BigDecimal("50.00")
        val req = UpdateProductRequest(price = newUnitPrice)
        val updatedProduct1 = productSteps.update(req, product1.id!!)

        val totalAmountAfterCheckout = updatedProduct1.price * 2 + product2.price
        totalAmountAfterCheckout shouldNotBe totalAmountBeforeCheckout

        shouldThrow<SomeProductsHaveChangedException> {
            orderService.checkout(userId)
        }

        val order = orderRepository.findDraftByCustomerId(userId)
            .shouldNotBeNull()

        order.status shouldBe OrderStatus.DRAFT
        order.totalAmount shouldBe totalAmountAfterCheckout

        val orderItem = order.items
            .firstOrNull { it.productId == product1.id }
            .shouldNotBeNull()
        orderItem.unitPrice.amount shouldBe newUnitPrice
    }

    @Test
    fun `should remove deleted product from cart on checkout`() {
        val product1 = productSteps.create(sellerId = userId)
        val product2 = productSteps.create(sellerId = userId)

        orderService.addItemToCart(product1.id!!, userId)
        orderService.addItemToCart(product2.id!!, userId)

        productService.delete(product2.id!!, userId)

        shouldThrow<SomeProductsHaveChangedException> {
            orderService.checkout(userId)
        }

        val order = orderRepository.findDraftByCustomerId(userId)
            .shouldNotBeNull()

        order.status shouldBe OrderStatus.DRAFT
        order.totalAmount shouldBe product1.price
        order.items shouldHaveSize 1
        order.items.first().productId shouldBe product1.id
    }

    @Test
    fun `should throw when order is empty on checkout`() {
        val product1 = productSteps.create(sellerId = userId)
        orderService.addItemToCart(product1.id!!, userId)
        orderService.deleteItemFromCart(product1.id!!, userId)
        shouldThrow<EmptyOrderException> {
            orderService.checkout(userId)
        }
    }

    @Test
    fun `should create draft order and add items to cart`() {
        val product1 = productSteps.create(sellerId = userId)
        val product2 = productSteps.create(sellerId = userId)
        val product3 = productSteps.create(sellerId = userId)

        orderRepository.findDraftByCustomerId(userId)
            .shouldBeNull()

        repeat(2) {
            orderService.addItemToCart(product1.id!!, userId)
        }
        orderService.addItemToCart(product2.id!!, userId)
        orderService.addItemToCart(product3.id!!, userId)

        val order = orderRepository.findDraftByCustomerId(userId)
            .shouldNotBeNull()
        order.updatedAt.shouldNotBeNull()
        order.status shouldBe OrderStatus.DRAFT

        val orderItems = order.items
        orderItems.shouldHaveSize(3)
        orderItems.forAll {
            it.orderId shouldBe order.id
        }
        order.totalAmount.amount shouldBe orderItems.sumOf { it.totalPrice().amount }

        val orderItem1 = orderItems.single { it.productId == product1.id }
        val orderItem2 = orderItems.single { it.productId == product2.id }
        val orderItem3 = orderItems.single { it.productId == product3.id }

        orderItem1.productId shouldBe product1.id
        orderItem2.productId shouldBe product2.id
        orderItem3.productId shouldBe product3.id

        orderItem1.quantity shouldBe 2
        orderItem2.quantity shouldBe 1
        orderItem3.quantity shouldBe 1
    }

    @Test
    fun `should delete item from cart when quantity is one`() {
        val product = productSteps.create(sellerId = userId)

        orderService.addItemToCart(product.id!!, userId)

        orderService.deleteItemFromCart(product.id!!, userId)

        val order = orderRepository.findDraftByCustomerId(userId)
            .shouldNotBeNull()

        order.totalAmount shouldBe Money.ZERO

        order.items
            .firstOrNull { it.productId == product.id }
            .shouldBeNull()
    }

    @Test
    fun `should decrement quantity for item in cart`() {
        val product = productSteps.create(sellerId = userId)

        repeat(3) {
            orderService.addItemToCart(product.id!!, userId)
        }

        orderService.deleteItemFromCart(product.id!!, userId)

        val order = orderRepository.findDraftByCustomerId(userId)
            .shouldNotBeNull()

        order.totalAmount shouldBe product.price * 2

        val item = order.items
            .firstOrNull { it.productId == product.id }
            .shouldNotBeNull()

        item.quantity shouldBe 2
    }

    @Test
    fun `should throw when delete item from cart and order does not exist`() {
        val product = productSteps.create(sellerId = userId)
        shouldThrow<OrderNotFoundException> {
            orderService.deleteItemFromCart(product.id!!, userId)
        }
    }

    @Test
    fun `should throw when delete item from cart and item not found`() {
        val product1 = productSteps.create(sellerId = userId)
        val product2 = productSteps.create(sellerId = userId)
        orderService.addItemToCart(product1.id!!, userId)

        shouldThrow<OrderItemNotFoundException> {
            orderService.deleteItemFromCart(product2.id!!, userId)
        }
    }

    @Test
    fun `should return all items in cart`() {
        val product1 = productSteps.create(sellerId = userId)
        val product2 = productSteps.create(sellerId = userId)

        repeat(2) {
            orderService.addItemToCart(product1.id!!, userId)
        }
        orderService.addItemToCart(product2.id!!, userId)

        val order = orderRepository.findDraftByCustomerId(userId)
            .shouldNotBeNull()

        val orderItems = orderService.getCartItems(userId)
        orderItems.shouldHaveSize(2)
        order.totalAmount.amount shouldBe orderItems.sumOf { it.totalPrice().amount }

        val orderItem1 = orderItems.single { it.productId == product1.id }
        val orderItem2 = orderItems.single { it.productId == product2.id }

        orderItem1.quantity shouldBe 2
        orderItem2.quantity shouldBe 1
    }

    @Test
    fun `should throw when get order which does not exist`() {
        shouldThrow<OrderNotFoundException> {
            orderService.getCartItems(userId)
        }
    }
}
