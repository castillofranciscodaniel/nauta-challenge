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

        return Flux.fromIterable(orders)
            .flatMap { order ->
                orderRepository.findByPurchaseNumberAndBookingId(order.purchaseNumber, bookingId)
                    .switchIfEmpty(
                        orderRepository.save(order.copy(bookingId = bookingId))
                            .doOnSuccess { saved -> logger.info("[saveOrdersForBooking] Created new order: id=${saved.id}, purchaseNumber=${saved.purchaseNumber}") }
                    )
                    .doOnSuccess { existing ->
                        if (existing != null && existing.id != null) {
                            logger.debug("[saveOrdersForBooking] Order already exists: id=${existing.id}, purchaseNumber=${existing.purchaseNumber}")
                        }
                    }
                    .flatMap { orderToUse ->
                        invoiceService.saveInvoicesForOrder(order.invoices, orderToUse.id!!)
                            .map { invoicesSaved ->
                                orderToUse.copy(invoices = invoicesSaved)
                            }
                    }
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
