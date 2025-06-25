package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.ContainerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ContainerController(
    private val containerService: ContainerService
) {
    private val logger = LoggerFactory.getLogger(ContainerController::class.java)

    @GetMapping("/containers")
    suspend fun getAllContainers(): Flow<Container> {
        logger.info("[getAllContainers] Recuperando todos los contenedores del usuario actual")

        return containerService.findAllContainersForCurrentUser()
            .catch { e ->
                logger.error("[getAllContainers] Error en el flujo de recuperación de contenedores", e)
                throw e
            }
    }

    @GetMapping("/containers/{containerId}/orders")
    suspend fun getOrdersByContainerId(@PathVariable containerId: String): Flow<Order> {
        logger.info("[getOrdersByContainerId] Recuperando órdenes para el contenedor: {}", containerId)

        return containerService.findOrdersByContainerId(containerId)
            .catch { e ->
                logger.error("[getOrdersByContainerId] Error en el flujo de recuperación de órdenes para contenedor ID: {}", containerId, e)
                throw e
            }
    }
}
