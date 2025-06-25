package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// OrderService.kt
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val invoiceService: InvoiceService,
    private val userLoggedService: UserLoggedService,
    private val containerRepository: ContainerRepository
) {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    suspend fun saveOrdersForBooking(orders: List<Order>, bookingId: Long): List<Order> {
        logger.info("[saveOrdersForBooking] Guardando {} órdenes para booking ID: {}", orders.size, bookingId)

        return try {
            val result = orders.map { order ->
                logger.debug("[saveOrdersForBooking] Procesando orden con número de compra: {}", order.purchaseNumber)

                val orderToUse = orderRepository.findByPurchaseNumberAndBookingId(order.purchaseNumber, bookingId)
                    ?: orderRepository.save(order.copy(bookingId = bookingId)).also {
                        logger.debug("[saveOrdersForBooking] Nueva orden creada con ID: {}", it.id)
                    }

                val invoicesSaved = invoiceService.saveInvoicesForOrder(order.invoices, orderToUse.id!!)

                orderToUse.copy(invoices = invoicesSaved)
            }

            logger.info("[saveOrdersForBooking] {} órdenes guardadas exitosamente", result.size)
            result
        } catch (e: Exception) {
            logger.error("[saveOrdersForBooking] Error al guardar órdenes para booking ID: {}", bookingId, e)
            throw e
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun findAllOrdersForCurrentUser(): Flow<Order> {
        val currentUser = userLoggedService.getCurrentUser()
        logger.info("[findAllOrdersForCurrentUser] Buscando órdenes para usuario ID: {}", currentUser.id)

        // Get all orders for the user with a single query
        return orderRepository.findAllByUserId(currentUser.id!!)
            .map { order ->
                logger.debug("[findAllOrdersForCurrentUser] Cargando facturas para orden ID: {}", order.id)
                // For each order, load its associated invoices
                val invoices = invoiceService.findAllByOrderId(order.id!!).toList()
                order.copy(invoices = invoices)
            }
            .catch { e ->
                logger.error("[findAllOrdersForCurrentUser] Error al recuperar órdenes para usuario ID: {}", currentUser.id, e)
                throw e
            }
    }

    suspend fun findContainersByOrderId(purchaseNumber: String): Flow<Container> {
        val currentUser = userLoggedService.getCurrentUser()
        logger.info("[findContainersByOrderId] Buscando contenedores para compra: {} (usuario: {})", purchaseNumber, currentUser.id)

        return containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, currentUser.id!!)
            .catch { e ->
                logger.error("[findContainersByOrderId] Error al recuperar contenedores para compra: {}", purchaseNumber, e)
                throw e
            }
    }
}
