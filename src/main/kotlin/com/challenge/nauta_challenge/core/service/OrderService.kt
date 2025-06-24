package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// OrderService.kt
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val invoiceService: InvoiceService,
    private val bookingRepository: BookingRepository,
    private val userLoggedService: UserLoggedService,
    private val containerRepository: ContainerRepository
) {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    fun saveOrdersForBooking(orders: List<Order>, bookingId: Long): Mono<List<Order>> {
        logger.info("[saveOrdersForBooking] Saving ${orders.size} orders for bookingId=$bookingId")

        if (orders.isEmpty()) {
            return Mono.just(emptyList())
        }

        return Flux.fromIterable(orders)
            .concatMap { order ->
                // Primero buscar si la orden ya existe
                orderRepository.findByPurchaseNumberAndBookingId(order.purchaseNumber, bookingId)
                    .flatMap { existingOrder ->
                        // La orden existe, procesamos solo las facturas
                        logger.debug("[saveOrdersForBooking] Order already exists: id=${existingOrder.id}, purchaseNumber=${existingOrder.purchaseNumber}")

                        // Guardar facturas asociadas a la orden existente
                        invoiceService.saveInvoicesForOrder(order.invoices, existingOrder.id!!)
                            .map { savedInvoices ->
                                existingOrder.copy(invoices = savedInvoices)
                            }
                    }
                    .switchIfEmpty(
                        // La orden no existe, creamos una nueva
                        Mono.defer {
                            val orderToSave = order.copy(bookingId = bookingId)
                            orderRepository.save(orderToSave)
                                .flatMap { savedOrder ->
                                    logger.info("[saveOrdersForBooking] Created new order: id=${savedOrder.id}, purchaseNumber=${savedOrder.purchaseNumber}")

                                    // Guardar facturas asociadas a la nueva orden
                                    invoiceService.saveInvoicesForOrder(order.invoices, savedOrder.id!!)
                                        .map { savedInvoices ->
                                            savedOrder.copy(invoices = savedInvoices)
                                        }
                                }
                        }
                    )
            }
            .collectList()
            .doOnSuccess { savedOrders -> logger.info("[saveOrdersForBooking] Successfully processed ${savedOrders.size} orders for bookingId=$bookingId") }
            .doOnError { error -> logger.error("[saveOrdersForBooking] Error saving orders for bookingId=$bookingId", error) }
    }

    fun findAllOrdersForCurrentUser(): Flux<Order> {
        logger.info("[findAllOrdersForCurrentUser] Fetching all orders for current user")

        return userLoggedService.getCurrentUserId()
            .flatMapMany { user ->
                logger.debug("[findAllOrdersForCurrentUser] Found current user, id=${user.id}")

                orderRepository.findAllByUserId(user.id!!)
                    .flatMap { order ->
                        logger.debug("[findAllOrdersForCurrentUser] Processing order: id=${order.id}, purchaseNumber=${order.purchaseNumber}")

                        // For each order, load its associated invoices
                        invoiceService.findAllByOrderId(order.id!!)
                            .collectList()
                            .map { invoices ->
                                logger.debug("[findAllOrdersForCurrentUser] Loaded ${invoices.size} invoices for order ${order.id}")
                                order.copy(invoices = invoices)
                            }
                    }
                    .doOnComplete { logger.info("[findAllOrdersForCurrentUser] Finished retrieving orders for user ${user.id}") }
            }
            .doOnError { error -> logger.error("[findAllOrdersForCurrentUser] Error fetching orders for current user", error) }
    }

    fun findContainersByOrderId(purchaseNumber: String): Flux<Container> {
        logger.info("[findContainersByOrderId] Looking for containers by purchaseNumber=$purchaseNumber")

        return userLoggedService.getCurrentUserId()
            .flatMapMany { user ->
                logger.debug("[findContainersByOrderId] Found current user, id=${user.id}")

                containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, user.id!!)
                    .doOnComplete { logger.info("[findContainersByOrderId] Finished retrieving containers for purchaseNumber=$purchaseNumber") }
            }
            .doOnError { error -> logger.error("[findContainersByOrderId] Error fetching containers by purchaseNumber=$purchaseNumber", error) }
    }
}
