package com.daniel.marketplaceapp.order.repository

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.enums.OrderStatus
import jakarta.persistence.OptimisticLockException
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.UUID
import kotlin.collections.contains

@Repository
@ConditionalOnProperty(name = ["app.service-type.db"], havingValue = "jooq")
class JooqOrderRepository(
    private val dsl: DSLContext,
) : OrderRepository {
    override fun save(order: Order) =
        if (order.id == null) {
            insert(order)
        } else {
            update(order)
        }

    private fun insert(order: Order): Order {
        val orderId = UUID.randomUUID()
        dsl.insertInto(ORDERS)
            .set(ORDER_ID, orderId)
            .set(ORDER_STATUS, order.status.name)
            .set(ORDER_TOTAL_AMOUNT, order.totalAmount.amount)
            .set(ORDER_CUSTOMER_ID, order.customerId)
            .set(ORDER_CREATED_AT, Timestamp.from(order.createdAt))
            .set(ORDER_UPDATED_AT, order.updatedAt?.let { Timestamp.from(it) })
            .set(ORDER_VERSION, 0L)
            .execute()
        order.id = orderId
        order.version = 0L
        insertItems(order.items, orderId)
        return order
    }

    private fun update(order: Order): Order {
        val updateRows = dsl.update(ORDERS)
            .set(ORDER_STATUS, order.status.name)
            .set(ORDER_TOTAL_AMOUNT, order.totalAmount.amount)
            .set(ORDER_UPDATED_AT, order.updatedAt?.let { Timestamp.from(it) })
            .set(ORDER_VERSION, order.version?.inc())
            .where(ORDER_ID.eq(order.id))
            .and(ORDER_VERSION.eq(order.version))
            .execute()
        if (updateRows == 0) {
            throw OptimisticLockException("Row was updated or deleted by another transaction")
        }
        val fetchedItems = findAllItemsByOrderId(requireNotNull(order.id))

        val updateDiff = handleDiff(fetchedItems, order.items)
        if (updateDiff.forInsert.isNotEmpty()) {
            insertItems(updateDiff.forInsert, requireNotNull(order.id))
        }
        if (updateDiff.forDelete.isNotEmpty()) {
            deleteItems(updateDiff.forDelete)
        }
        if (updateDiff.forUpdate.isNotEmpty()) {
            updateItems(updateDiff.forUpdate)
        }
        return order
    }

    private fun findAllItemsByOrderId(orderId: UUID) =
        dsl.selectFrom(ORDER_ITEMS)
            .where(ITEM_ORDER_ID.eq(orderId))
            .forUpdate()
            .fetch()
            .map { toOrderItemDomain(it) }

    private fun insertItems(items: List<OrderItem>, orderId: UUID) {
        val queries = items.map { item ->
            val itemId = UUID.randomUUID()
            item.id = itemId
            item.orderId = orderId
            dsl.insertInto(ORDER_ITEMS)
                .set(ITEM_ID, itemId)
                .set(ITEM_ORDER_ID, orderId)
                .set(ITEM_PRODUCT_ID, item.productId)
                .set(ITEM_QUANTITY, item.quantity)
                .set(ITEM_UNIT_PRICE, item.unitPrice.amount)
        }
        dsl.batch(queries).execute()
    }

    private fun deleteItems(items: List<OrderItem>) {
        val ids = items.map { it.id }
        dsl.deleteFrom(ORDER_ITEMS)
            .where(ITEM_ID.`in`(ids))
            .execute()
    }

    private fun updateItems(items: List<OrderItem>) {
        val queries = items.map { item ->
            dsl.update(ORDER_ITEMS)
                .set(ITEM_QUANTITY, item.quantity)
                .set(ITEM_UNIT_PRICE, item.unitPrice.amount)
                .where(ITEM_ID.eq(item.id))
        }
        dsl.batch(queries).execute()
    }

    override fun findDraftByCustomerId(customerId: UUID): Order? {
        val records = dsl.select()
            .from(ORDERS)
            .leftJoin(ORDER_ITEMS).on(ORDER_ID.eq(ITEM_ORDER_ID))
            .where(ORDER_CUSTOMER_ID.eq(customerId))
            .and(ORDER_STATUS.eq(OrderStatus.DRAFT.name))
            .fetch()
            .ifEmpty { return null }

        val order = toOrderDomain(records.first())
        if (isCartEmpty(records)) return order

        records.forEach { record ->
            order.items.add(toOrderItemDomain(record))
        }
        return order
    }

    private fun toOrderDomain(record: Record) = Order(
        id = requireNotNull(record.get(ORDER_ID)),
        status = OrderStatus.valueOf(requireNotNull(record.get(ORDER_STATUS))),
        totalAmount = Money(requireNotNull(record.get(ORDER_TOTAL_AMOUNT))),
        customerId = requireNotNull(record.get(ORDER_CUSTOMER_ID)),
        createdAt = requireNotNull(record.get(ORDER_CREATED_AT)).toInstant(),
        updatedAt = record.get(ORDER_UPDATED_AT)?.toInstant(),
        items = mutableListOf(),
        version = record.get(ORDER_VERSION)
    )

    private fun toOrderItemDomain(record: Record) = OrderItem(
        id = requireNotNull(record.get(ITEM_ID)),
        orderId = requireNotNull(record.get(ITEM_ORDER_ID)),
        productId = requireNotNull(record.get(ITEM_PRODUCT_ID)),
        unitPrice = Money(requireNotNull(record.get(ITEM_UNIT_PRICE))),
        quantity = requireNotNull(record.get(ITEM_QUANTITY)),
    )

    private fun handleDiff(
        fetchedItems: MutableList<OrderItem>,
        newItems: MutableList<OrderItem>
    ): UpdateDiff {
        val fetchedItemsByIds = fetchedItems.associateBy { it.id }
        val newItemsIdSet = newItems.mapNotNull { it.id }.toSet()

        val forInsert = newItems.filter { it.id == null || it.id !in fetchedItemsByIds.keys }
        val forDelete = fetchedItems.filter { i -> i.id !in newItemsIdSet }
        val forUpdate = newItems.filter { item ->
            val oldItem = fetchedItemsByIds[item.id]
            oldItem != null && !item.hasSamePersistentStateAs(oldItem)
        }
        return UpdateDiff(
            forInsert = forInsert,
            forDelete = forDelete,
            forUpdate = forUpdate
        )
    }

    private fun isCartEmpty(records: List<Record>) =
        records.size == 1 && records.first().get(ITEM_ID) == null

    private fun OrderItem.hasSamePersistentStateAs(other: OrderItem) =
        productId == other.productId &&
                unitPrice == other.unitPrice &&
                quantity == other.quantity

    companion object {
        private val ORDERS = DSL.table(DSL.name("orders"))
        private val ORDER_ID = DSL.field(DSL.name("orders","id"), UUID::class.java)
        private val ORDER_STATUS = DSL.field(DSL.name("status"), String::class.java)
        private val ORDER_TOTAL_AMOUNT = DSL.field(DSL.name("total_amount"), BigDecimal::class.java)
        private val ORDER_CUSTOMER_ID = DSL.field(DSL.name("customer_id"), UUID::class.java)
        private val ORDER_CREATED_AT = DSL.field(DSL.name("created_at"), Timestamp::class.java)
        private val ORDER_UPDATED_AT = DSL.field(DSL.name("updated_at"), Timestamp::class.java)
        private val ORDER_VERSION = DSL.field(DSL.name("version"), Long::class.java)

        private val ORDER_ITEMS = DSL.table(DSL.name("order_items"))
        private val ITEM_ID = DSL.field(DSL.name("order_items", "id"), UUID::class.java)
        private val ITEM_ORDER_ID = DSL.field(DSL.name("order_id"), UUID::class.java)
        private val ITEM_PRODUCT_ID = DSL.field(DSL.name("product_id"), UUID::class.java)
        private val ITEM_UNIT_PRICE = DSL.field(DSL.name("unit_price"), BigDecimal::class.java)
        private val ITEM_QUANTITY = DSL.field(DSL.name("quantity"), Int::class.java)
    }
}

data class UpdateDiff(
    val forInsert: List<OrderItem>,
    val forDelete: List<OrderItem>,
    val forUpdate: List<OrderItem>
)
