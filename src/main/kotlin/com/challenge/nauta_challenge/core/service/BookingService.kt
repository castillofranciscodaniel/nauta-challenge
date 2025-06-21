package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import org.springframework.stereotype.Service

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val containerRepository: ContainerRepository,
    private val orderRepository: OrderRepository,
    private val userLoggedService: UserLoggedService,
    private val invoiceRepository: InvoiceRepository
) {

    suspend fun saveBooking(booking: Booking): Booking {
        // Obtener userId del usuario autenticado
        val userLogged = userLoggedService.getCurrentUserId()

        val bookingSaved = bookingRepository.findByBookingNumberAndUserId(
            booking.bookingNumber, userLogged.id!!
        ) ?: bookingRepository.save(booking)

        val containersSaved = booking.containers.map { containerRepository.save(it) }

        val ordersSaved = booking.orders.map { order ->
            val orderSaved = orderRepository.save(order)
            val invoicesSaved = order.invoices.map {
                val invoiceWithOrderId = it.copy(orderId = orderSaved.id)
                invoiceRepository.save(invoiceWithOrderId)
            }
            orderSaved.copy(invoices = invoicesSaved)
        }


        return bookingSaved.copy(containers = containersSaved, orders = ordersSaved)
    }
}