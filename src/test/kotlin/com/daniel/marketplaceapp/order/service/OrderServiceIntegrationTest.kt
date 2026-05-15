package com.daniel.marketplaceapp.order.service

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.order.exception.OrderItemNotFoundException
import com.daniel.marketplaceapp.order.exception.OrderNotFoundException
import com.daniel.marketplaceapp.order.repository.OrderRepository
import com.daniel.marketplaceapp.testsupport.steps.ProductSteps
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.Test

@Transactional
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderServiceIntegrationTest {
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
    fun `should create draft order and add items to cart`() {
        val product1 = productSteps.createProduct(sellerId = userId)
        val product2 = productSteps.createProduct(sellerId = userId)
        val product3 = productSteps.createProduct(sellerId = userId)

        orderRepository.findDraftByCustomerIdWithItems(userId)
            .shouldBeNull()

        repeat(2) {
            orderService.addItemToCart(product1.id!!, userId)
        }
        orderService.addItemToCart(product2.id!!, userId)
        orderService.addItemToCart(product3.id!!, userId)

        val order = orderRepository.findDraftByCustomerIdWithItems(userId)
            .shouldNotBeNull()
        order.updatedAt.shouldNotBeNull()
        order.status shouldBe OrderStatus.DRAFT

        val orderItems = order.items
        orderItems.shouldHaveSize(3)
        orderItems.forAll {
            it.order?.id shouldBe order.id
        }
        order.totalAmount.amount shouldBe orderItems.sumOf { it.totalPrice().amount }

        val orderItem1 = orderItems.single { it.product.id == product1.id }
        val orderItem2 = orderItems.single { it.product.id == product2.id }
        val orderItem3 = orderItems.single { it.product.id == product3.id }

        orderItem1.product.id shouldBe product1.id
        orderItem2.product.id shouldBe product2.id
        orderItem3.product.id shouldBe product3.id

        orderItem1.quantity shouldBe 2
        orderItem2.quantity shouldBe 1
        orderItem3.quantity shouldBe 1
    }

    @Test
    fun `should delete item from cart when quantity is one`() {
        val product = productSteps.createProduct(sellerId = userId)

        orderService.addItemToCart(product.id!!, userId)

        orderService.deleteItemFromCart(product.id!!, userId)

        val order = orderRepository.findDraftByCustomerIdWithItems(userId)
            .shouldNotBeNull()

        order.totalAmount shouldBe Money.ZERO

        order.items
            .firstOrNull { it.product.id == product.id }
            .shouldBeNull()
    }

    @Test
    fun `should decrement quantity for item in cart`() {
        val product = productSteps.createProduct(sellerId = userId)

        repeat(3) {
            orderService.addItemToCart(product.id!!, userId)
        }

        orderService.deleteItemFromCart(product.id!!, userId)

        val order = orderRepository.findDraftByCustomerIdWithItems(userId)
            .shouldNotBeNull()

        order.totalAmount shouldBe product.price * 2

        val item = order.items
            .firstOrNull { it.product.id == product.id }
            .shouldNotBeNull()

        item.quantity shouldBe 2
    }

    @Test
    fun `should throw when delete item from cart and order does not exist`() {
        val product = productSteps.createProduct(sellerId = userId)
        shouldThrow<OrderNotFoundException> {
            orderService.deleteItemFromCart(product.id!!, userId)
        }
    }

    @Test
    fun `should throw when delete item from cart and item not found`() {
        val product1 = productSteps.createProduct(sellerId = userId)
        val product2 = productSteps.createProduct(sellerId = userId)
        orderService.addItemToCart(product1.id!!, userId)

        shouldThrow<OrderItemNotFoundException> {
            orderService.deleteItemFromCart(product2.id!!, userId)
        }
    }

    @Test
    fun `should return all items in cart`() {
        val product1 = productSteps.createProduct(sellerId = userId)
        val product2 = productSteps.createProduct(sellerId = userId)

        repeat(2) {
            orderService.addItemToCart(product1.id!!, userId)
        }
        orderService.addItemToCart(product2.id!!, userId)

        val order = orderRepository.findDraftByCustomerIdWithItems(userId)
            .shouldNotBeNull()

        val orderItems = orderService.getCartItems(userId)
        orderItems.shouldHaveSize(2)
        order.totalAmount.amount shouldBe orderItems.sumOf { it.totalPrice().amount }

        val orderItem1 = orderItems.single { it.product.id == product1.id }
        val orderItem2 = orderItems.single { it.product.id == product2.id }

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