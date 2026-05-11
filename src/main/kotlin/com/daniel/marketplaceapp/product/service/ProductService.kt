package com.daniel.marketplaceapp.product.service

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.product.dto.CreateProductRequest
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.exception.EmptyUpdateProductRequestException
import com.daniel.marketplaceapp.product.exception.ProductAlreadyDeletedException
import com.daniel.marketplaceapp.product.exception.ProductNotFoundException
import com.daniel.marketplaceapp.product.mapper.updateFrom
import com.daniel.marketplaceapp.product.model.Product
import com.daniel.marketplaceapp.product.repository.ProductRepository
import com.daniel.marketplaceapp.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val userService: UserService
) {
    @Transactional
    fun create(req: CreateProductRequest, sellerId: UUID): Product {
        val seller = userService.findByIdOrThrow(sellerId)
        val product = Product(
            title = req.title,
            description = req.description,
            price = Money(req.price),
            seller = seller,
            status = ProductStatus.ACTIVE,
        )
        return productRepository.save(product)
    }

    @Transactional
    fun update(req: UpdateProductRequest, productId: UUID, sellerId: UUID): Product {
        checkUpdateReqIsEmpty(req)
        val product = findByIdAndSellerIdOrThrow(productId, sellerId)
        checkProductNotDeletedOrThrow(product)
        product.updateFrom(req)
        return product
    }

    @Transactional
    fun delete(productId: UUID, sellerId: UUID) {
        val product = findByIdAndSellerIdOrThrow(productId, sellerId)
        product.status = ProductStatus.DELETED
        product.updatedAt = Instant.now()
    }

    @Transactional
    fun hide(productId: UUID, currentUserId: UUID) {
        updateStatus(productId, currentUserId, ProductStatus.HIDDEN)
    }

    @Transactional
    fun show(productId: UUID, currentUserId: UUID) {
        updateStatus(productId, currentUserId, ProductStatus.ACTIVE)
    }

    fun findAll() : List<Product> {
        return productRepository.findAllByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE)
    }

    fun findAllBySellerId(sellerId: UUID, currentUserId: UUID): List<Product> {
        val visibleStatuses = getVisibleStatuses(sellerId, currentUserId)
        return productRepository.findAllBySellerIdAndStatusInOrderByCreatedAtDesc(sellerId, visibleStatuses)
    }

    private fun findByIdAndSellerIdOrThrow(id: UUID, sellerId: UUID) =
        productRepository.findByIdAndSellerId(id, sellerId)
            ?: throw ProductNotFoundException("Product with id $id not found")

    private fun getVisibleStatuses(sellerId: UUID, currentUserId: UUID) =
        if (sellerId == currentUserId) {
            listOf(ProductStatus.ACTIVE, ProductStatus.HIDDEN)
        } else {
            listOf(ProductStatus.ACTIVE)
        }

    private fun updateStatus(
        productId: UUID,
        currentUserId: UUID,
        newStatus: ProductStatus
    ) {
        val product = findByIdAndSellerIdOrThrow(productId, currentUserId)
        checkProductNotDeletedOrThrow(product)
        product.status = newStatus
        product.updatedAt = Instant.now()
    }

    private fun checkUpdateReqIsEmpty(req: UpdateProductRequest) {
        if (req.title == null && req.description == null && req.price == null) {
            throw EmptyUpdateProductRequestException("Update product request is empty")
        }
    }

    private fun checkProductNotDeletedOrThrow(product: Product) {
        if (product.status == ProductStatus.DELETED) {
            throw ProductAlreadyDeletedException("Product ${product.id} has been deleted")
        }
    }
}
