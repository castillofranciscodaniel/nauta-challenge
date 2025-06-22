package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

// OrderService.kt
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val invoiceService: InvoiceService,
    private val bookingRepository: BookingRepository,
    private val userLoggedService: UserLoggedService
) {
    suspend fun saveOrdersForBooking(orders: List<Order>, bookingId: Long): List<Order> {
        return orders.map { order ->
            val orderToUse = orderRepository.findByPurchaseNumberAndBookingId(order.purchaseNumber, bookingId)
                ?: orderRepository.save(order.copy(bookingId = bookingId))

            val invoicesSaved = invoiceService.saveInvoicesForOrder(order.invoices, orderToUse.id!!)

            orderToUse.copy(invoices = invoicesSaved)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun findAllOrdersForCurrentUser(): Flow<Order> {
        val currentUser = userLoggedService.getCurrentUserId()

        // Obtenemos todos los bookings del usuario
        return bookingRepository.findAllByUserId(currentUser.id!!)
            .flatMapMerge { booking ->
                // Para cad booking, obtenemos sus órdenes
                orderRepository.findAllByBookingId(booking.id!!)
            }
            .map { order ->
                // Para cada orden, si tiene ID, obtenemos sus facturas
                // Obtenemos las facturas como Flow
                val invoices = invoiceService.findAllByOrderId(order.id!!).toList()
                order.copy(invoices = invoices)
            }
    }
}
