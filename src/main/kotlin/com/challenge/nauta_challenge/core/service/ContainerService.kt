package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

// ContainerService.kt
@Service
class ContainerService(
    private val containerRepository: ContainerRepository,
    private val userLoggedService: UserLoggedService,
    private val bookingRepository: BookingRepository,
    private val orderRepository: OrderRepository,
    private val orderContainerRepository: OrderContainerRepository,
    private val invoiceService: InvoiceService
) {
    suspend fun saveContainersForBooking(containers: List<Container>, bookingId: Long): List<Container> {
        return containers.map { container ->
            containerRepository.findByContainerNumberAndBookingId(container.containerNumber, bookingId)
                ?: containerRepository.save(container.copy(bookingId = bookingId))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun findAllContainersForCurrentUser(): Flow<Container> {
        val currentUser = userLoggedService.getCurrentUserId()

        // Obtenemos todos los bookings del usuario
        return bookingRepository.findAllByUserId(currentUser.id!!)
            .flatMapMerge { booking ->
                // Para cada booking, obtenemos sus contenedores
                containerRepository.findAllByBookingId(booking.id!!)
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun findOrdersByContainerId(containerId: String): Flow<Order> {
        val currentUser = userLoggedService.getCurrentUserId()

        return orderRepository.findOrdersByContainerIdAndUserId(containerId, currentUser.id!!)
            .map { order ->
                // Para cada orden, cargar sus facturas asociadas
                val invoices = invoiceService.findAllByOrderId(order.id!!).toList()
                order.copy(invoices = invoices)
            }
    }
}
