package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.ContainerService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api")
class ContainerController(
    private val containerService: ContainerService
) {
    private val logger = LoggerFactory.getLogger(ContainerController::class.java)

    @GetMapping("/containers")
    fun getAllContainers(): Flux<Container> {
        logger.info("[getAllContainers] Request received for all containers")

        return containerService.findAllContainersForCurrentUser()
            .doOnSubscribe { logger.debug("[getAllContainers] Starting to stream containers") }
            .doOnComplete { logger.info("[getAllContainers] Completed streaming all containers") }
            .doOnError { error -> logger.error("[getAllContainers] Error retrieving containers", error) }
    }

    @GetMapping("/containers/{containerId}/orders")
    fun getOrdersByContainerId(@PathVariable containerId: String): Flux<Order> {
        logger.info("[getOrdersByContainerId] Request received for orders by containerId: $containerId")

        return containerService.findOrdersByContainerId(containerId)
            .doOnSubscribe { logger.debug("[getOrdersByContainerId] Starting to stream orders for containerId: $containerId") }
            .doOnComplete { logger.info("[getOrdersByContainerId] Completed streaming orders for containerId: $containerId") }
            .doOnError { error -> logger.error("[getOrdersByContainerId] Error retrieving orders for containerId: $containerId", error) }
    }
}
