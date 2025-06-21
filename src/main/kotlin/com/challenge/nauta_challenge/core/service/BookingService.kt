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
        val userId = userLoggedService.getCurrentUserId().id!!

        // Verificar o crear booking
        val bookingToSave = booking.copy(userId = userId)
        val bookingSaved = bookingRepository.findByBookingNumberAndUserId(
            booking.bookingNumber, userId
        ) ?: bookingRepository.save(bookingToSave)

        // Guardar contenedores si no existen ya
        val containersSaved = booking.containers.map { container ->
            val existing = containerRepository.findByContainerNumberAndBookingId(
                container.containerNumber, bookingSaved.id!!)
            existing ?: containerRepository.save(container.copy(bookingId = bookingSaved.id))
        }

        // Guardar órdenes si no existen ya
        val ordersSaved = booking.orders.map { order ->
            val existingOrder = orderRepository.findByPurchaseNumberAndBookingId(order.purchaseNumber, bookingSaved.id!!)
            val orderToUse = existingOrder ?: orderRepository.save(order.copy(bookingId = bookingSaved.id))

            // Guardar invoices si no existen
            val invoicesSaved = order.invoices.map { invoice ->
                invoiceRepository.save(invoice.copy(orderId = orderToUse.id))
            }

            orderToUse.copy(invoices = invoicesSaved)
        }

        // Aplicar reglas de asociación orden ↔ contenedor
        if (ordersSaved.size == 1 && containersSaved.isNotEmpty()) {
            val order = ordersSaved.first()
            containersSaved.forEach { container ->
                if (!orderContainerRepository.existsByOrderIdAndContainerId(order.id!!, container.id!!)) {
                    orderContainerRepository.save(order.id, container.id)
                }
            }
        } else if (containersSaved.size == 1 && ordersSaved.isNotEmpty()) {
            val container = containersSaved.first()
            ordersSaved.forEach { order ->
                if (!orderContainerRepository.existsByOrderIdAndContainerId(order.id!!, container.id!!)) {
                    orderContainerRepository.save(order.id, container.id)
                }
            }
        } else {
            println("Relación ambigua en booking ${booking.bookingNumber}. No se crean asociaciones automáticas.")
        }

        return bookingSaved.copy(containers = containersSaved, orders = ordersSaved)
    }
}