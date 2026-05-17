package com.daniel.marketplaceapp.product.repository

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.product.domain.Product
import com.daniel.marketplaceapp.product.enums.ProductStatus
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*

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
            .set(ID, id)
            .set(PRICE, product.price.amount)
            .set(TITLE, product.title)
            .set(DESCRIPTION, product.description)
            .set(SELLER_ID, product.sellerId)
            .set(STATUS, product.status.name)
            .set(CREATED_AT, Timestamp.from(product.createdAt))
            .set(UPDATED_AT, product.updatedAt?.let { Timestamp.from(it) })
            .execute()
        product.id = id
        return product
    }

    private fun update(product: Product): Product {
        dsl.update(PRODUCTS)
            .set(TITLE, product.title)
            .set(DESCRIPTION, product.description)
            .set(PRICE, product.price.amount)
            .set(STATUS, product.status.name)
            .set(UPDATED_AT, product.updatedAt?.let { Timestamp.from(it) })
            .where(ID.eq(product.id))
            .execute()
        return product
    }

    override fun findById(id: UUID) =
        dsl.selectFrom(PRODUCTS)
            .where(ID.eq(id))
            .fetchOne()
            ?.toDomain()

    override fun findByIdAndSellerId(
        id: UUID,
        sellerId: UUID
    ) = dsl.selectFrom(PRODUCTS)
        .where(ID.eq(id))
        .and(SELLER_ID.eq(sellerId))
        .fetchOne()
        ?.toDomain()

    override fun findByIdAndStatus(
        id: UUID,
        status: ProductStatus
    ) = dsl.selectFrom(PRODUCTS)
        .where(ID.eq(id))
        .and(STATUS.eq(status.name))
        .fetchOne()
        ?.toDomain()

    override fun findAllByStatus(status: ProductStatus) =
        dsl.selectFrom(PRODUCTS)
            .where(STATUS.eq(status.name))
            .orderBy(CREATED_AT.desc())
            .fetch()
            .map { it.toDomain() }

    override fun findAllBySellerIdAndStatusList(
        sellerId: UUID,
        statusList: Collection<ProductStatus>
    ) = dsl.selectFrom(PRODUCTS)
        .where(SELLER_ID.eq(sellerId))
        .and(STATUS.`in`(statusList.map { it.name }))
        .orderBy(CREATED_AT.desc())
        .fetch()
        .map { it.toDomain() }

    private fun Record.toDomain() = Product(
        id = requireNotNull(get(ID)),
        title = requireNotNull(get(TITLE)),
        description = get(DESCRIPTION),
        price = Money(requireNotNull(get(PRICE))),
        sellerId = requireNotNull(get(SELLER_ID)),
        status = ProductStatus.valueOf(requireNotNull(get(STATUS))),
        createdAt = requireNotNull(get(CREATED_AT)).toInstant(),
        updatedAt = get(UPDATED_AT)?.toInstant(),
    )

    companion object {
        private val PRODUCTS = DSL.table(DSL.name("products"))
        private val ID = DSL.field(DSL.name("id"), UUID::class.java)
        private val TITLE = DSL.field(DSL.name("title"), String::class.java)
        private val PRICE = DSL.field(DSL.name("price"), BigDecimal::class.java)
        private val DESCRIPTION = DSL.field(DSL.name("description"), String::class.java)
        private val SELLER_ID = DSL.field(DSL.name("seller_id"), UUID::class.java)
        private val STATUS = DSL.field(DSL.name("status"), String::class.java)
        private val CREATED_AT = DSL.field(DSL.name("created_at"), Timestamp::class.java)
        private val UPDATED_AT = DSL.field(DSL.name("updated_at"), Timestamp::class.java)
    }
}
