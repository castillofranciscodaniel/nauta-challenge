package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// OrderContainerAssociationService.kt
@Service
class OrderContainerAssociationService(private val orderContainerRepository: OrderContainerRepository) {
    private val logger = LoggerFactory.getLogger(OrderContainerAssociationService::class.java)

    fun createAssociations(orders: List<Order>, containers: List<Container>, bookingNumber: String): Mono<Void> {
        logger.info("[createAssociations] Creating associations for booking: $bookingNumber, orders: ${orders.size}, containers: ${containers.size}")

        return when {
            orders.size == 1 && containers.isNotEmpty() -> {
                logger.debug("[createAssociations] One order to many containers case detected")
                associateOneOrderToManyContainers(orders.first(), containers)
                    .doOnSuccess { logger.info("[createAssociations] Successfully associated one order to ${containers.size} containers") }
            }

            containers.size == 1 && orders.isNotEmpty() -> {
                logger.debug("[createAssociations] Many orders to one container case detected")
                associateManyOrdersToOneContainer(orders, containers.first())
                    .doOnSuccess { logger.info("[createAssociations] Successfully associated ${orders.size} orders to one container") }
            }

            else -> {
                logger.warn("[createAssociations] Ambiguous relationship in booking $bookingNumber. No automatic associations created.")
                Mono.empty()
            }
        }
            .doOnError { error -> logger.error("[createAssociations] Error creating associations for booking $bookingNumber", error) }
            .then()
    }

    private fun associateOneOrderToManyContainers(order: Order, containers: List<Container>): Mono<Void> {
        return Flux.fromIterable(containers)
            .flatMap { container ->
                orderContainerRepository.existsByOrderIdAndContainerId(order.id!!, container.id!!)
                    .flatMap { exists ->
                        if (!exists) {
                            logger.debug("[associateOneOrderToManyContainers] Creating association: orderId=${order.id}, containerId=${container.id}")
                            orderContainerRepository.save(order.id, container.id)
                                .doOnSuccess { logger.debug("[associateOneOrderToManyContainers] Association created successfully") }
                                .doOnError { error -> logger.error("[associateOneOrderToManyContainers] Error creating association", error) }
                                .then()
                        } else {
                            logger.debug("[associateOneOrderToManyContainers] Association already exists: orderId=${order.id}, containerId=${container.id}")
                            Mono.empty()
                        }
                    }
            }
            .then()
    }

    private fun associateManyOrdersToOneContainer(orders: List<Order>, container: Container): Mono<Void> {
        return Flux.fromIterable(orders)
            .flatMap { order ->
                orderContainerRepository.existsByOrderIdAndContainerId(order.id!!, container.id!!)
                    .flatMap { exists ->
                        if (!exists) {
                            logger.debug("[associateManyOrdersToOneContainer] Creating association: orderId=${order.id}, containerId=${container.id}")
                            orderContainerRepository.save(order.id, container.id)
                                .doOnSuccess { logger.debug("[associateManyOrdersToOneContainer] Association created successfully") }
                                .doOnError { error -> logger.error("[associateManyOrdersToOneContainer] Error creating association", error) }
                                .then()
                        } else {
                            logger.debug("[associateManyOrdersToOneContainer] Association already exists: orderId=${order.id}, containerId=${container.id}")
                            Mono.empty()
                        }
                    }
            }
            .then()
    }
}