package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.OrderRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class OrderRepositoryImpl(
    private val orderDao: OrderDao
) : OrderRepository {
    private val logger = LoggerFactory.getLogger(OrderRepositoryImpl::class.java)

    override fun save(order: Order): Mono<Order> {
        logger.info("[save] Attempting to save order: purchaseNumber=${order.purchaseNumber}, bookingId=${order.bookingId}")

        val orderEntity = OrderEntity.fromModel(order)
        return orderDao.save(orderEntity)
            .map { it.toModel() }
            .doOnSuccess { logger.info("[save] Successfully saved order: id=${it.id}, purchaseNumber=${it.purchaseNumber}") }
            .switchIfEmpty(Mono.error(ModelNotSavedException("Order not saved")))
            .onErrorMap { e ->
                logger.error("[save] Error while saving order: purchaseNumber=${order.purchaseNumber}", e)
                ModelNotSavedException("Order not saved: ${e.message}")
            }
    }

    override fun findByPurchaseNumberAndBookingId(purchaseNumber: String, bookingId: Long): Mono<Order> {
        logger.debug("[findByPurchaseNumberAndBookingId] Looking for order: purchaseNumber=$purchaseNumber, bookingId=$bookingId")

        return orderDao.findByPurchaseNumberAndBookingId(purchaseNumber, bookingId)
            .map { it.toModel() }
            .doOnSuccess { order ->
                if (order != null) {
                    logger.debug("[findByPurchaseNumberAndBookingId] Found order: id=${order.id}")
                } else {
                    logger.debug("[findByPurchaseNumberAndBookingId] Order not found: purchaseNumber=$purchaseNumber, bookingId=$bookingId")
                }
            }
            .onErrorMap { e ->
                logger.warn("[findByPurchaseNumberAndBookingId] Error looking for order: purchaseNumber=$purchaseNumber, bookingId=$bookingId", e)
                RepositoryException("Error finding order", e)
            }
    }

    override fun findOrdersByContainerIdAndUserId(containerId: String, userId: Long): Flux<Order> {
        logger.debug("[findOrdersByContainerIdAndUserId] Looking for orders by container: containerId=$containerId, userId=$userId")

        return orderDao.findOrdersByContainerIdAndUserId(containerId, userId)
            .map { it.toModel() }
            .doOnSubscribe { logger.debug("[findOrdersByContainerIdAndUserId] Starting order search for containerId=$containerId") }
            .doOnComplete { logger.debug("[findOrdersByContainerIdAndUserId] Completed order search for containerId=$containerId") }
            .onErrorMap { e ->
                logger.error("[findOrdersByContainerIdAndUserId] Error retrieving orders by container: containerId=$containerId", e)
                RepositoryException("Error retrieving orders by container", e)
            }
    }

    override fun findAllByUserId(userId: Long): Flux<Order> {
        logger.debug("[findAllByUserId] Looking for all orders for userId=$userId")

        return orderDao.findAllByUserId(userId)
            .map { it.toModel() }
            .doOnSubscribe { logger.debug("[findAllByUserId] Starting order search for userId=$userId") }
            .doOnComplete { logger.debug("[findAllByUserId] Completed order search for userId=$userId") }
            .onErrorMap { e ->
                logger.error("[findAllByUserId] Error retrieving orders for userId=$userId", e)
                RepositoryException("Error retrieving orders for user", e)
            }
    }
}