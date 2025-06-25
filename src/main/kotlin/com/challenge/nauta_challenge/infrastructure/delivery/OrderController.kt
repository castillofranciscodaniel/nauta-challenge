package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.OrderService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    private val logger = LoggerFactory.getLogger(OrderController::class.java)

    @GetMapping()
    suspend fun getAllOrders(): Flow<Order> {
        logger.info("[getAllOrders] Recuperando todas las órdenes del usuario actual")

        return orderService.findAllOrdersForCurrentUser()
            .catch { e ->
                logger.error("[getAllOrders] Error en el flujo de recuperación de órdenes", e)
                throw e
            }
    }

    @GetMapping("/{purchaseId}/containers")
    suspend fun getContainersByOrderId(@PathVariable purchaseId: String): Flow<Container> {
        logger.info("[getContainersByOrderId] Recuperando contenedores para la orden: {}", purchaseId)

        return orderService.findContainersByOrderId(purchaseId)
            .catch { e ->
                logger.error("[getContainersByOrderId] Error en el flujo de recuperación de contenedores para orden ID: {}", purchaseId, e)
                throw e
            }
    }
}
