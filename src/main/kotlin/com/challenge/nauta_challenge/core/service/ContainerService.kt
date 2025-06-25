package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ContainerService(
    private val containerRepository: ContainerRepository,
    private val userLoggedService: UserLoggedService,
    private val orderRepository: OrderRepository,
    private val invoiceService: InvoiceService
) {
    private val logger = LoggerFactory.getLogger(ContainerService::class.java)

    suspend fun saveContainersForBooking(containers: List<Container>, bookingId: Long): List<Container> {
        logger.info("[saveContainersForBooking] Guardando {} contenedores para booking ID: {}", containers.size, bookingId)

        return try {
            throw RuntimeException("Simulación de error para pruebas") // Simulación de error para pruebas
            val result = containers.map { container ->
                containerRepository.findByContainerNumberAndBookingId(container.containerNumber, bookingId)
                    ?: containerRepository.save(container.copy(bookingId = bookingId))
            }

            logger.debug("[saveContainersForBooking] Contenedores guardados: {}", result.size)
            result
        } catch (e: Exception) {
            logger.error("[saveContainersForBooking] Error al guardar contenedores para booking ID: {}", bookingId, e)
            throw e
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun findAllContainersForCurrentUser(): Flow<Container> {
        val currentUser = userLoggedService.getCurrentUser()
        logger.info("[findAllContainersForCurrentUser] Buscando contenedores para usuario ID: {}", currentUser.id)

        return containerRepository.findAllByUserId(currentUser.id!!)
            .catch { e ->
                logger.error("[findAllContainersForCurrentUser] Error recuperando contenedores para usuario ID: {}", currentUser.id, e)
                throw e
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun findOrdersByContainerId(containerId: String): Flow<Order> {
        val currentUser = userLoggedService.getCurrentUser()
        logger.info("[findOrdersByContainerId] Buscando órdenes para contenedor ID: {} (usuario: {})", containerId, currentUser.id)

        return orderRepository.findOrdersByContainerIdAndUserId(containerId, currentUser.id!!)
            .map { order ->
                val invoices = invoiceService.findAllByOrderId(order.id!!).toList()
                order.copy(invoices = invoices)
            }
            .catch { e ->
                logger.error("[findOrdersByContainerId] Error recuperando órdenes para contenedor ID: {}", containerId, e)
                throw e
            }
    }
}