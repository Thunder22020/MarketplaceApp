package com.daniel.marketplaceapp.product.controller

import com.daniel.marketplaceapp.product.dto.CreateProductRequest
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.repository.ProductRepository
import com.daniel.marketplaceapp.security.dto.AccessTokenResponse
import com.daniel.marketplaceapp.security.util.JwtConstants
import com.daniel.marketplaceapp.testsupport.annotations.ControllerIntegrationTest
import com.daniel.marketplaceapp.testsupport.fixtures.TOO_SHORT_VALUE
import com.daniel.marketplaceapp.testsupport.fixtures.randomPassword
import com.daniel.marketplaceapp.testsupport.fixtures.randomString
import com.daniel.marketplaceapp.testsupport.fixtures.randomUsername
import com.daniel.marketplaceapp.testsupport.steps.ProductSteps
import com.daniel.marketplaceapp.testsupport.steps.UserSteps
import com.daniel.marketplaceapp.user.domain.User
import com.daniel.marketplaceapp.user.dto.request.LoginRequest
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@ControllerIntegrationTest
class ProductControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userSteps: UserSteps

    @Autowired
    private lateinit var productSteps: ProductSteps

    @Autowired
    private lateinit var productRepository: ProductRepository

    private lateinit var userA: User
    private lateinit var userB: User
    private lateinit var userAToken: String
    private lateinit var userBToken: String

    @BeforeAll
    fun setUp() {
        val passwordA = randomPassword()
        val passwordB = randomPassword()
        userA = userSteps.createUser(username = randomUsername(), password = passwordA)
        userB = userSteps.createUser(username = randomUsername(), password = passwordB)
        userAToken = login(userA.username, passwordA)
        userBToken = login(userB.username, passwordB)
    }

    @Test
    fun `should create product`() {
        val request = CreateProductRequest(
            title = randomString(),
            description = randomString(),
            price = BigDecimal("150.00")
        )

        mockMvc.perform(
            post(PRODUCTS_URL)
                .auth(userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.title").value(request.title))
            .andExpect(jsonPath("$.description").value(request.description))
            .andExpect(jsonPath("$.price").value(150.00))
            .andExpect(jsonPath("$.sellerId").value(userA.id.toString()))
            .andExpect(jsonPath("$.status").value(ProductStatus.ACTIVE.name))
    }

    @Test
    fun `should return bad request when create product title is invalid`() {
        val request = CreateProductRequest(
            title = TOO_SHORT_VALUE,
            description = randomString(),
            price = BigDecimal("100.00")
        )

        mockMvc.perform(
            post(PRODUCTS_URL)
                .auth(userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details[0].field").value("title"))
    }

    @Test
    fun `should update product`() {
        val product = productSteps.createProduct(sellerId = requireNotNull(userA.id))
        val req = UpdateProductRequest(
            title = randomString(),
            description = randomString(),
            price = BigDecimal("150.00")
        )

        mockMvc.perform(
            put("$PRODUCTS_URL/${product.id}")
                .auth(userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(product.id.toString()))
            .andExpect(jsonPath("$.title").value(req.title))
            .andExpect(jsonPath("$.description").value(req.description))
            .andExpect(jsonPath("$.price").value(150.00))
            .andExpect(jsonPath("$.status").value(ProductStatus.ACTIVE.name))
    }

    @Test
    fun `should return bad request when update request is empty`() {
        val product = productSteps.createProduct(sellerId = requireNotNull(userA.id))

        mockMvc.perform(
            put("$PRODUCTS_URL/${product.id}")
                .auth(userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateProductRequest()))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("PRODUCT_UPDATE_REQUEST_EMPTY"))
    }

    @Test
    fun `should return not found when update product by non seller`() {
        val product = productSteps.createProduct(sellerId = requireNotNull(userA.id))
        val req = UpdateProductRequest(title = randomString())

        mockMvc.perform(
            put("$PRODUCTS_URL/${product.id}")
                .auth(userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
    }

    @Test
    fun `should soft delete product`() {
        val product = productSteps.createProduct(sellerId = requireNotNull(userA.id))

        mockMvc.perform(
            delete("$PRODUCTS_URL/${product.id}")
                .auth(userAToken)
        )
            .andExpect(status().isNoContent)

        val deletedProduct = productRepository.findById(requireNotNull(product.id))
            .shouldNotBeNull()
        deletedProduct.status shouldBe ProductStatus.DELETED
    }

    @Test
    fun `should hide product`() {
        val product = productSteps.createProduct(sellerId = requireNotNull(userA.id))

        mockMvc.perform(
            patch("$PRODUCTS_URL/${product.id}/hide")
                .auth(userAToken)
        )
            .andExpect(status().isNoContent)

        val hiddenProduct = productRepository.findById(requireNotNull(product.id))
            .shouldNotBeNull()
        hiddenProduct.status shouldBe ProductStatus.HIDDEN
    }

    @Test
    fun `should unhide product`() {
        val product = productSteps.createProduct(sellerId = requireNotNull(userA.id))
        productSteps.hideProduct(requireNotNull(product.id), requireNotNull(userA.id))

        mockMvc.perform(
            patch("$PRODUCTS_URL/${product.id}/unhide")
                .auth(userAToken)
        )
            .andExpect(status().isNoContent)

        val activeProduct = productRepository.findById(requireNotNull(product.id))
            .shouldNotBeNull()
        activeProduct.status shouldBe ProductStatus.ACTIVE
    }

    @Test
    fun `should return unauthorized when token is missing`() {
        mockMvc.perform(get(PRODUCTS_URL))
            .andExpect(status().isUnauthorized)
    }

    private fun login(username: String, password: String): String {
        val response = mockMvc.perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest(username, password)))
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        return objectMapper.readValue(response, AccessTokenResponse::class.java).accessToken
    }

    private fun MockHttpServletRequestBuilder.auth(
        token: String
    ) = header(HttpHeaders.AUTHORIZATION, "${JwtConstants.BEARER_PREFIX}$token")

    companion object {
        private const val PRODUCTS_URL = "/products"
        private const val LOGIN_URL = "/api/auth/login"
    }
}
