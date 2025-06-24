package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// ContainerService.kt
@Service
class ContainerService(
    private val containerRepository: ContainerRepository,
    private val userLoggedService: UserLoggedService,
    private val orderRepository: OrderRepository,
    private val invoiceService: InvoiceService
) {
    private val logger = LoggerFactory.getLogger(ContainerService::class.java)

    fun saveContainersForBooking(containers: List<Container>, bookingId: Long): Mono<List<Container>> {
        logger.info("[saveContainersForBooking] Saving ${containers.size} containers for bookingId=$bookingId")

        return Flux.fromIterable(containers)
            .flatMap { container ->
                containerRepository.findByContainerNumberAndBookingId(container.containerNumber, bookingId)
                    .map { existingContainer ->
                        logger.debug("[saveContainersForBooking] Container already exists: id=${existingContainer.id}, containerNumber=${existingContainer.containerNumber}")
                        existingContainer
                    }
                    .switchIfEmpty(
                        Mono.defer {
                            containerRepository.save(container.copy(bookingId = bookingId))
                                .doOnSuccess { saved ->
                                    logger.info("[saveContainersForBooking] Saved container: id=${saved.id}, containerNumber=${saved.containerNumber}")
                                }
                        }
                    )
            }
            .collectList()
            .doOnSuccess { savedList -> logger.info("[saveContainersForBooking] Successfully saved/found ${savedList.size} containers") }
            .onErrorMap { e ->
                logger.error("[saveContainersForBooking] Error saving containers for bookingId=$bookingId", e)
                RuntimeException("Failed to save containers: ${e.message}", e)
            }
    }

    fun findAllContainersForCurrentUser(): Flux<Container> {
        logger.info("[findAllContainersForCurrentUser] Fetching all containers for current user")

        return userLoggedService.getCurrentUserId()
            .flatMapMany { user ->
                logger.debug("[findAllContainersForCurrentUser] Found current user, id=${user.id}")
                containerRepository.findAllByUserId(user.id!!)
                    .doOnComplete { logger.info("[findAllContainersForCurrentUser] Finished retrieving containers for user ${user.id}") }
            }
            .onErrorResume { e ->
                logger.error("[findAllContainersForCurrentUser] Error fetching containers for current user", e)
                Flux.error(RuntimeException("Error fetching containers: ${e.message}", e))
            }
    }

    fun findOrdersByContainerId(containerId: String): Flux<Order> {
        logger.info("[findOrdersByContainerId] Looking for orders by containerId=$containerId")

        return userLoggedService.getCurrentUserId()
            .flatMapMany { user ->
                logger.debug("[findOrdersByContainerId] Found current user, id=${user.id}")
                orderRepository.findOrdersByContainerIdAndUserId(containerId, user.id!!)
                    .flatMap { order ->
                        logger.debug("[findOrdersByContainerId] Found order: id=${order.id}, purchaseNumber=${order.purchaseNumber}")
                        // For each order, load its associated invoices
                        invoiceService.findAllByOrderId(order.id!!)
                            .collectList()
                            .map { invoices ->
                                logger.debug("[findOrdersByContainerId] Loaded ${invoices.size} invoices for order ${order.id}")
                                order.copy(invoices = invoices)
                            }
                    }
                    .doOnComplete { logger.info("[findOrdersByContainerId] Completed fetching orders for containerId=$containerId") }
            }
            .onErrorResume { e ->
                logger.error("[findOrdersByContainerId] Error fetching orders for containerId=$containerId", e)
                Flux.error(RuntimeException("Error fetching orders for container: ${e.message}", e))
            }
    }
}
