package com.daniel.marketplaceapp.payment.service

import com.daniel.marketplaceapp.order.domain.Order
import com.daniel.marketplaceapp.order.enums.OrderStatus
import com.daniel.marketplaceapp.payment.domain.Payment
import com.daniel.marketplaceapp.payment.dto.CreatePaymentProviderRequest
import com.daniel.marketplaceapp.payment.enums.PaymentStatus
import com.daniel.marketplaceapp.payment.exception.PaymentCannotBeCreatedException
import com.daniel.marketplaceapp.payment.exception.PaymentCreationFailedException
import com.daniel.marketplaceapp.payment.provider.PaymentProviderClient
import com.daniel.marketplaceapp.payment.repository.PaymentRepository
import java.time.Instant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentProviderClient: PaymentProviderClient,
) {
    @Transactional(noRollbackFor = [PaymentCreationFailedException::class])
    fun createPaymentForOrder(order: Order): Payment {
        if (order.status != OrderStatus.PENDING_PAYMENT || order.id == null) {
            throw PaymentCannotBeCreatedException("Order not in PENDING_PAYMENT status or order id is null")
        }

        val payment = getInitPayment(order)
        val response = try {
            paymentProviderClient.createPayment(
                CreatePaymentProviderRequest(
                    amount = payment.amount,
                    description = "Payment for order ${order.id}",
                )
            )
        } catch (_: Exception) {
            payment.markFailed()
            paymentRepository.save(payment)
            throw PaymentCreationFailedException("Payment creation failed for order ${order.id}")
        }
        payment.markPending(
            response.confirmationUrl,
            response.externalId,
        )
        return paymentRepository.save(payment)
    }

    private fun getInitPayment(order: Order) = Payment(
        id = null,
        orderId = requireNotNull(order.id),
        externalId = null,
        confirmationUrl = null,
        amount = order.totalAmount,
        status = PaymentStatus.INIT,
        createdAt = Instant.now(),
        updatedAt = null,
    )
}
