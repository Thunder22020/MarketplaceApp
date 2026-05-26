package com.daniel.marketplaceapp.product.service

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.product.domain.Product
import com.daniel.marketplaceapp.product.dto.CreateProductRequest
import com.daniel.marketplaceapp.product.dto.UpdateProductRequest
import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.exception.EmptyUpdateProductRequestException
import com.daniel.marketplaceapp.product.exception.PageNumberIsNegativeException
import com.daniel.marketplaceapp.product.exception.ProductNotFoundException
import com.daniel.marketplaceapp.product.repository.ProductRepository
import java.time.Instant
import java.util.UUID
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun create(req: CreateProductRequest, sellerId: UUID): Product {
        val product = createProductFromReq(req, sellerId)
        return productRepository.save(product)
    }

    @Transactional
    fun update(req: UpdateProductRequest, productId: UUID, sellerId: UUID): Product {
        checkUpdateReqIsEmpty(req)
        val product = getByIdAndSellerIdOrThrow(productId, sellerId)
        product.update(req)
        return productRepository.save(product)
    }

    @Transactional
    fun delete(productId: UUID, sellerId: UUID) {
        val product = getByIdAndSellerIdOrThrow(productId, sellerId)
        product.deleteProduct()
        productRepository.save(product)
    }

    @Transactional
    fun hide(productId: UUID, currentUserId: UUID) {
        updateStatus(productId, currentUserId, ProductStatus.HIDDEN)
    }

    @Transactional
    fun unhide(productId: UUID, currentUserId: UUID) {
        updateStatus(productId, currentUserId, ProductStatus.ACTIVE)
    }

    @Transactional(readOnly = true)
    fun getAll(page: Int = 0): List<Product> {
        validatePageParam(page)
        val products = productRepository.findAllByStatus(
            PageRequest.of(page, PAGE_SIZE),
            ProductStatus.ACTIVE
        )
        return products
    }

    @Transactional(readOnly = true)
    fun getAllBySellerId(sellerId: UUID, currentUserId: UUID, page: Int = 0): List<Product> {
        validatePageParam(page)
        val pageable = PageRequest.of(page, PAGE_SIZE)
        val visibleStatuses = getVisibleStatuses(sellerId, currentUserId)
        val products = productRepository.findAllBySellerIdAndStatusList(
            sellerId,
            pageable,
            visibleStatuses
        )
        return products
    }

    fun getAllByIds(ids: Collection<UUID>) = productRepository.findAllByIds(ids)

    fun getByIdOrThrow(id: UUID) =
        productRepository.findById(id)
            ?: throw ProductNotFoundException("Product with id $id not found")

    private fun getByIdAndSellerIdOrThrow(id: UUID, sellerId: UUID) =
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
        val product = getByIdAndSellerIdOrThrow(productId, currentUserId)
        product.updateStatus(newStatus)
        productRepository.save(product)
    }

    private fun createProductFromReq(
        req: CreateProductRequest,
        sellerId: UUID
    ) = Product(
        title = req.title,
        description = req.description,
        price = Money(req.price),
        status = ProductStatus.ACTIVE,
        id = null,
        sellerId = sellerId,
        createdAt = Instant.now(),
        updatedAt = null,
    )

    private fun checkUpdateReqIsEmpty(req: UpdateProductRequest) {
        if (req.title == null && req.description == null && req.price == null) {
            throw EmptyUpdateProductRequestException("Update product request is empty")
        }
    }

    private fun validatePageParam(page: Int) {
        if (page < 0) {
            throw PageNumberIsNegativeException("Page must be greater than zero")
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
