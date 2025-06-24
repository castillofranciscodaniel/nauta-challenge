package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

// OrderService.kt
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val invoiceService: InvoiceService,
    private val bookingRepository: BookingRepository,
    private val userLoggedService: UserLoggedService,
    private val containerRepository: ContainerRepository
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

        // Get all orders for the user with a single query
        return orderRepository.findAllByUserId(currentUser.id!!)
            .map { order ->
                // For each order, load its associated invoices
                val invoices = invoiceService.findAllByOrderId(order.id!!).toList()
                order.copy(invoices = invoices)
            }
    }

    suspend fun findContainersByOrderId(purchaseNumber: String): Flow<Container> {
        val currentUser = userLoggedService.getCurrentUserId()

        return containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, currentUser.id!!)
    }

}
