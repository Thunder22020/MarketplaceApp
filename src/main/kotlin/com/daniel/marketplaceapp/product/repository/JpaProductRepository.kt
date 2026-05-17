package com.daniel.marketplaceapp.product.repository

import com.daniel.marketplaceapp.product.domain.Product
import com.daniel.marketplaceapp.product.enums.ProductStatus
import com.daniel.marketplaceapp.product.mapper.toDomain
import com.daniel.marketplaceapp.product.mapper.toEntity
import com.daniel.marketplaceapp.user.exception.UserNotFoundException
import com.daniel.marketplaceapp.user.repository.UserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@ConditionalOnProperty(name = ["app.service-type.db"], havingValue = "jpa", matchIfMissing = true)
class JpaProductRepository(
    private val springDataProductRepository: SpringDataProductRepository,
    private val userRepository: UserRepository,
): ProductRepository {
    override fun save(product: Product): Product {
        val seller = findSellerOrThrow(product.sellerId)
        val productEntity = product.toEntity(seller)
        return springDataProductRepository.save(productEntity).toDomain()
    }

    override fun findById(id: UUID) =
        springDataProductRepository.findByIdOrNull(id)?.toDomain()

    private fun findSellerOrThrow(sellerId: UUID) =
        userRepository.findByIdOrNull(sellerId)
            ?: throw UserNotFoundException("Seller with id $sellerId not found")

    override fun findByIdAndSellerId(
        id: UUID,
        sellerId: UUID
    ) = springDataProductRepository.findByIdAndSellerId(id, sellerId)?.toDomain()

    override fun findByIdAndStatus(
        id: UUID,
        status: ProductStatus
    ) = springDataProductRepository.findByIdAndStatusIs(id, status)?.toDomain()

    override fun findAllByStatus(status: ProductStatus) =
        springDataProductRepository.findAllByStatusOrderByCreatedAtDesc(status).map { it.toDomain() }

    override fun findAllBySellerIdAndStatusList(
        sellerId: UUID,
        statusList: Collection<ProductStatus>
    ) = springDataProductRepository.findAllBySellerIdAndStatusInOrderByCreatedAtDesc(sellerId, statusList)
        .map { it.toDomain() }
}
