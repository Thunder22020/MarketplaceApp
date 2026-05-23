package com.daniel.marketplaceapp.order.repository

import com.daniel.marketplaceapp.core.domain.Money
import com.daniel.marketplaceapp.core.mapper.toInstantUtc
import com.daniel.marketplaceapp.core.mapper.toLocalDateTime
import com.daniel.marketplaceapp.jooq.Tables.ORDERS
import com.daniel.marketplaceapp.jooq.Tables.ORDER_ITEMS
import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.domain.OrderItem
import com.daniel.marketplaceapp.order.enums.OrderStatus
import java.util.UUID
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository

@Repository
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
            .set(ORDERS.ID, orderId)
            .set(ORDERS.STATUS, order.status.name)
            .set(ORDERS.TOTAL_AMOUNT, order.totalAmount.amount)
            .set(ORDERS.CUSTOMER_ID, order.customerId)
            .set(ORDERS.CREATED_AT, order.createdAt.toLocalDateTime())
            .set(ORDERS.UPDATED_AT, order.updatedAt?.toLocalDateTime())
            .set(ORDERS.VERSION, 0L)
            .execute()
        order.id = orderId
        order.version = 0L
        insertItems(order.items, orderId)
        return order
    }

    private fun update(order: Order): Order {
        val newVersion = order.version?.inc()
        val updateRows = dsl.update(ORDERS)
            .set(ORDERS.STATUS, order.status.name)
            .set(ORDERS.TOTAL_AMOUNT, order.totalAmount.amount)
            .set(ORDERS.UPDATED_AT, order.updatedAt?.toLocalDateTime())
            .set(ORDERS.VERSION, newVersion)
            .where(ORDERS.ID.eq(order.id))
            .and(ORDERS.VERSION.eq(order.version))
            .execute()
        if (updateRows == 0) {
            throw OptimisticLockingFailureException("Row was updated or deleted by another transaction")
        }
        order.version = newVersion

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
            .where(ORDER_ITEMS.ORDER_ID.eq(orderId))
            .forUpdate()
            .fetch()
            .map { toOrderItemDomain(it) }

    private fun insertItems(items: List<OrderItem>, orderId: UUID) {
        val queries = items.map { item ->
            item.orderId = orderId
            dsl.insertInto(ORDER_ITEMS)
                .set(ORDER_ITEMS.ORDER_ID, orderId)
                .set(ORDER_ITEMS.PRODUCT_ID, item.productId)
                .set(ORDER_ITEMS.QUANTITY, item.quantity)
                .set(ORDER_ITEMS.UNIT_PRICE, item.unitPrice.amount)
        }
        dsl.batch(queries).execute()
    }

    private fun deleteItems(items: List<OrderItem>) {
        val queries = items.map { item ->
            dsl.deleteFrom(ORDER_ITEMS)
                .where(
                    ORDER_ITEMS.ORDER_ID.eq(item.orderId)
                        .and(ORDER_ITEMS.PRODUCT_ID.eq(item.productId))
                )
        }
        dsl.batch(queries).execute()
    }

    private fun updateItems(items: List<OrderItem>) {
        val queries = items.map { item ->
            dsl.update(ORDER_ITEMS)
                .set(ORDER_ITEMS.QUANTITY, item.quantity)
                .set(ORDER_ITEMS.UNIT_PRICE, item.unitPrice.amount)
                .where(
                    ORDER_ITEMS.ORDER_ID.eq(item.orderId)
                        .and(ORDER_ITEMS.PRODUCT_ID.eq(item.productId))
                )
        }
        dsl.batch(queries).execute()
    }

    override fun findDraftByCustomerId(customerId: UUID) =
        findByCustomerIdAndStatus(customerId, OrderStatus.DRAFT)

    override fun findPendingByCustomerId(customerId: UUID) =
        findByCustomerIdAndStatus(customerId, OrderStatus.PENDING_PAYMENT)

    private fun findByCustomerIdAndStatus(customerId: UUID, status: OrderStatus): Order? {
        val records = dsl.select()
            .from(ORDERS)
            .leftJoin(ORDER_ITEMS).on(ORDERS.ID.eq(ORDER_ITEMS.ORDER_ID))
            .where(ORDERS.CUSTOMER_ID.eq(customerId))
            .and(ORDERS.STATUS.eq(status.name))
            .fetch()
            .ifEmpty { return null }

        val order = toOrderDomain(records.first())
        if (isCartEmpty(records)) return order

        records.forEach { record ->
            order.items.add(toOrderItemDomain(record))
        }
        return order
    }

    override fun findById(id: UUID): Order? {
        val records = dsl.select()
            .from(ORDERS)
            .leftJoin(ORDER_ITEMS).on(ORDERS.ID.eq(ORDER_ITEMS.ORDER_ID))
            .where(ORDERS.ID.eq(id))
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
        id = requireNotNull(record.get(ORDERS.ID)),
        status = OrderStatus.valueOf(requireNotNull(record.get(ORDERS.STATUS))),
        totalAmount = Money(requireNotNull(record.get(ORDERS.TOTAL_AMOUNT))),
        customerId = requireNotNull(record.get(ORDERS.CUSTOMER_ID)),
        createdAt = requireNotNull(record.get(ORDERS.CREATED_AT)).toInstantUtc(),
        updatedAt = record.get(ORDERS.UPDATED_AT)?.toInstantUtc(),
        items = mutableListOf(),
        version = record.get(ORDERS.VERSION)
    )

    private fun toOrderItemDomain(record: Record) = OrderItem(
        orderId = requireNotNull(record.get(ORDER_ITEMS.ORDER_ID)),
        productId = requireNotNull(record.get(ORDER_ITEMS.PRODUCT_ID)),
        unitPrice = Money(requireNotNull(record.get(ORDER_ITEMS.UNIT_PRICE))),
        quantity = requireNotNull(record.get(ORDER_ITEMS.QUANTITY)),
    )

    private fun handleDiff(
        fetchedItems: MutableList<OrderItem>,
        newItems: MutableList<OrderItem>
    ): UpdateDiff {
        val fetchedItemsByProductIds = fetchedItems.associateBy { it.productId }
        val newItemsProductIdSet = newItems.map { it.productId }.toSet()

        val forInsert = newItems.filter { it.productId !in fetchedItemsByProductIds.keys }
        val forDelete = fetchedItems.filter { it.productId !in newItemsProductIdSet }
        val forUpdate = newItems.filter { item ->
            val oldItem = fetchedItemsByProductIds[item.productId]
            oldItem != null && !item.hasSamePersistentStateAs(oldItem)
        }
        return UpdateDiff(
            forInsert = forInsert,
            forDelete = forDelete,
            forUpdate = forUpdate
        )
    }

    private fun isCartEmpty(records: List<Record>) =
        records.size == 1 && records.first().get(ORDER_ITEMS.PRODUCT_ID) == null

    private fun OrderItem.hasSamePersistentStateAs(other: OrderItem) =
        productId == other.productId &&
                unitPrice == other.unitPrice &&
                quantity == other.quantity
}

data class UpdateDiff(
    val forInsert: List<OrderItem>,
    val forDelete: List<OrderItem>,
    val forUpdate: List<OrderItem>
)
