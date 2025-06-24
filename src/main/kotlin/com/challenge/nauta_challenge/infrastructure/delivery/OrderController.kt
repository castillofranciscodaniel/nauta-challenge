package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.OrderService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    private val logger = LoggerFactory.getLogger(OrderController::class.java)

    @GetMapping()
    fun getAllOrders(): Flux<Order> {
        logger.info("[getAllOrders] Request received for all orders")

        return orderService.findAllOrdersForCurrentUser()
            .doOnSubscribe { logger.debug("[getAllOrders] Starting to stream orders") }
            .doOnComplete { logger.info("[getAllOrders] Completed streaming all orders") }
            .doOnError { error -> logger.error("[getAllOrders] Error retrieving orders", error) }
    }

    @GetMapping("/{purchaseId}/containers")
    fun getContainersByOrderId(@PathVariable purchaseId: String): Flux<Container> {
        logger.info("[getContainersByOrderId] Request received for containers by purchaseId: $purchaseId")

        return orderService.findContainersByOrderId(purchaseId)
            .doOnSubscribe { logger.debug("[getContainersByOrderId] Starting to stream containers for purchaseId: $purchaseId") }
            .doOnComplete { logger.info("[getContainersByOrderId] Completed streaming containers for purchaseId: $purchaseId") }
            .doOnError { error -> logger.error("[getContainersByOrderId] Error retrieving containers for purchaseId: $purchaseId", error) }
    }
}
