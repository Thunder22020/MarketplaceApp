package com.daniel.marketplaceapp.product.repository

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.core.mapper.toInstantUtc
import com.daniel.marketplaceapp.core.mapper.toLocalDateTime
import com.daniel.marketplaceapp.jooq.Tables.PRODUCTS
import com.daniel.marketplaceapp.product.domain.Product
import com.daniel.marketplaceapp.product.enums.ProductStatus
import java.util.UUID
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(name = ["app.service-type.db"], havingValue = "jooq")
class JooqProductRepository(
    private val dsl: DSLContext,
) : ProductRepository {
    override fun save(product: Product) =
        if (product.id == null) {
            insert(product)
        } else {
            update(product)
        }

    private fun insert(product: Product): Product {
        val id = UUID.randomUUID()
        dsl.insertInto(PRODUCTS)
            .set(PRODUCTS.ID, id)
            .set(PRODUCTS.PRICE, product.price.amount)
            .set(PRODUCTS.TITLE, product.title)
            .set(PRODUCTS.DESCRIPTION, product.description)
            .set(PRODUCTS.SELLER_ID, product.sellerId)
            .set(PRODUCTS.STATUS, product.status.name)
            .set(PRODUCTS.CREATED_AT, product.createdAt.toLocalDateTime())
            .set(PRODUCTS.UPDATED_AT, product.updatedAt?.toLocalDateTime())
            .execute()
        product.id = id
        return product
    }

    private fun update(product: Product): Product {
        dsl.update(PRODUCTS)
            .set(PRODUCTS.TITLE, product.title)
            .set(PRODUCTS.DESCRIPTION, product.description)
            .set(PRODUCTS.PRICE, product.price.amount)
            .set(PRODUCTS.STATUS, product.status.name)
            .set(PRODUCTS.UPDATED_AT, product.updatedAt?.toLocalDateTime())
            .where(PRODUCTS.ID.eq(product.id))
            .execute()
        return product
    }

    override fun findById(id: UUID) =
        dsl.selectFrom(PRODUCTS)
            .where(PRODUCTS.ID.eq(id))
            .fetchOne()
            ?.toDomain()

    override fun findByIdAndSellerId(
        id: UUID,
        sellerId: UUID
    ) = dsl.selectFrom(PRODUCTS)
        .where(PRODUCTS.ID.eq(id))
        .and(PRODUCTS.SELLER_ID.eq(sellerId))
        .fetchOne()
        ?.toDomain()

    override fun findByIdAndStatus(
        id: UUID,
        status: ProductStatus
    ) = dsl.selectFrom(PRODUCTS)
        .where(PRODUCTS.ID.eq(id))
        .and(PRODUCTS.STATUS.eq(status.name))
        .fetchOne()
        ?.toDomain()

    override fun findAllByStatus(status: ProductStatus) =
        dsl.selectFrom(PRODUCTS)
            .where(PRODUCTS.STATUS.eq(status.name))
            .orderBy(PRODUCTS.CREATED_AT.desc())
            .fetch()
            .map { it.toDomain() }

    override fun findAllBySellerIdAndStatusList(
        sellerId: UUID,
        statusList: Collection<ProductStatus>
    ) = dsl.selectFrom(PRODUCTS)
        .where(PRODUCTS.SELLER_ID.eq(sellerId))
        .and(PRODUCTS.STATUS.`in`(statusList.map { it.name }))
        .orderBy(PRODUCTS.CREATED_AT.desc())
        .fetch()
        .map { it.toDomain() }

    private fun Record.toDomain() = Product(
        id = requireNotNull(get(PRODUCTS.ID)),
        title = requireNotNull(get(PRODUCTS.TITLE)),
        description = get(PRODUCTS.DESCRIPTION),
        price = Money(requireNotNull(get(PRODUCTS.PRICE))),
        sellerId = requireNotNull(get(PRODUCTS.SELLER_ID)),
        status = ProductStatus.valueOf(requireNotNull(get(PRODUCTS.STATUS))),
        createdAt = requireNotNull(get(PRODUCTS.CREATED_AT)).toInstantUtc(),
        updatedAt = get(PRODUCTS.UPDATED_AT)?.toInstantUtc(),
    )
}
