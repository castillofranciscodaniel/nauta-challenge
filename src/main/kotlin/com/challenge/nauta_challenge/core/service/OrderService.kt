package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.OrderRepository
import org.springframework.stereotype.Service

// OrderService.kt
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val invoiceService: InvoiceService
) {
    suspend fun saveOrdersForBooking(orders: List<Order>, bookingId: Long): List<Order> {
        return orders.map { order ->
            val orderToUse = orderRepository.findByPurchaseNumberAndBookingId(order.purchaseNumber, bookingId)
                ?: orderRepository.save(order.copy(bookingId = bookingId))

            val invoicesSaved = invoiceService.saveInvoicesForOrder(order.invoices, orderToUse.id!!)

            orderToUse.copy(invoices = invoicesSaved)
        }
    }
}
