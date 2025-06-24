package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

// ContainerService.kt
@Service
class ContainerService(
    private val containerRepository: ContainerRepository,
    private val userLoggedService: UserLoggedService,
    private val orderRepository: OrderRepository,
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

        // Get all containers for the user with a single query
        return containerRepository.findAllByUserId(currentUser.id!!)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun findOrdersByContainerId(containerId: String): Flow<Order> {
        val currentUser = userLoggedService.getCurrentUserId()

        return orderRepository.findOrdersByContainerIdAndUserId(containerId, currentUser.id!!)
            .map { order ->
                // For each order, load its associated invoices
                val invoices = invoiceService.findAllByOrderId(order.id!!).toList()
                order.copy(invoices = invoices)
            }
    }
}
