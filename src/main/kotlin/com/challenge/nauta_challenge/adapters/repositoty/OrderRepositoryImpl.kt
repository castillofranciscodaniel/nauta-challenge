package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.OrderRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class OrderRepositoryImpl(
    private val orderDao: OrderDao
) : OrderRepository {
    private val logger = LoggerFactory.getLogger(OrderRepositoryImpl::class.java)

    override suspend fun save(order: Order): Order {
        logger.info("[save] Attempting to save order: purchaseNumber=${order.purchaseNumber}, bookingId=${order.bookingId}")

        return try {
            val orderEntity = OrderEntity.fromModel(order)
            orderDao.save(orderEntity)
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.info("[save] Successfully saved order: id=${it.id}, purchaseNumber=${it.purchaseNumber}") }
                ?: throw ModelNotSavedException("Order not saved")
        } catch (e: Exception) {
            logger.error("[save] Error while saving order: purchaseNumber=${order.purchaseNumber}", e)
            throw ModelNotSavedException("Order not saved: ${e.message}")
        }
    }

    override suspend fun findByPurchaseNumberAndBookingId(purchaseNumber: String, bookingId: Long): Order? {
        logger.debug("[findByPurchaseNumberAndBookingId] Looking for order: purchaseNumber=$purchaseNumber, bookingId=$bookingId")

        return try {
            orderDao.findByPurchaseNumberAndBookingId(purchaseNumber, bookingId)
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.debug("[findByPurchaseNumberAndBookingId] Found order: id=${it.id}") }
                ?: run {
                    logger.debug("[findByPurchaseNumberAndBookingId] Order not found: purchaseNumber=$purchaseNumber, bookingId=$bookingId")
                    null
                }
        } catch (e: Exception) {
            logger.warn("[findByPurchaseNumberAndBookingId] Error looking for order: purchaseNumber=$purchaseNumber, bookingId=$bookingId", e)
            throw RepositoryException("Error finding order", e)
        }
    }

    override fun findOrdersByContainerIdAndUserId(containerId: String, userId: Long): Flow<Order> {
        logger.debug("[findOrdersByContainerIdAndUserId] Looking for orders by container: containerId=$containerId, userId=$userId")

        return try {
            orderDao.findOrdersByContainerIdAndUserId(containerId, userId)
                .map { it.toModel() }
                .asFlow()
                .onStart { logger.debug("[findOrdersByContainerIdAndUserId] Starting order search for containerId=$containerId") }
                .onCompletion { error ->
                    if (error == null) {
                        logger.debug("[findOrdersByContainerIdAndUserId] Completed order search for containerId=$containerId")
                    }
                }
        } catch (e: Exception) {
            logger.error("[findOrdersByContainerIdAndUserId] Error retrieving orders by container: containerId=$containerId", e)
            flow { throw RepositoryException("Error retrieving orders by container", e) }
        }
    }

    override fun findAllByUserId(userId: Long): Flow<Order> {
        logger.debug("[findAllByUserId] Looking for all orders for userId=$userId")

        return try {
            orderDao.findAllByUserId(userId)
                .map { it.toModel() }
                .asFlow()
                .onStart { logger.debug("[findAllByUserId] Starting order search for userId=$userId") }
                .onCompletion { error ->
                    if (error == null) {
                        logger.debug("[findAllByUserId] Completed order search for userId=$userId")
                    }
                }
        } catch (e: Exception) {
            logger.error("[findAllByUserId] Error retrieving orders for userId=$userId", e)
            flow { throw RepositoryException("Error retrieving orders for user", e) }
        }
    }
}