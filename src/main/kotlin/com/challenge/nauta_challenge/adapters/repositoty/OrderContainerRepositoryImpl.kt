package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.OrderContainer
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderContainerEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OrderContainerRepositoryImpl(
    private val orderContainerDao: OrderContainerDao
) : OrderContainerRepository {
    private val logger = LoggerFactory.getLogger(OrderContainerRepositoryImpl::class.java)

    override fun save(orderId: Long, containerId: Long): Mono<OrderContainer> {
        logger.info("[save] Attempting to save order-container relationship: orderId=$orderId, containerId=$containerId")

        val orderContainerEntity = OrderContainerEntity(orderId = orderId, containerId = containerId)
        return orderContainerDao.save(orderContainerEntity)
            .map { it.toModel() }
            .doOnSuccess { logger.info("[save] Successfully saved order-container relationship: orderId=$orderId, containerId=$containerId") }
            .switchIfEmpty(Mono.error(ModelNotSavedException("OrderContainer not saved")))
            .onErrorMap { e ->
                logger.error("[save] Error while saving order-container relationship: orderId=$orderId, containerId=$containerId", e)
                ModelNotSavedException("OrderContainer not saved: ${e.message}")
            }
    }

    override fun existsByOrderIdAndContainerId(orderId: Long, containerId: Long): Mono<Boolean> {
        logger.debug("[existsByOrderIdAndContainerId] Checking if relationship exists: orderId=$orderId, containerId=$containerId")

        return orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId)
            .defaultIfEmpty(false)
            .doOnSuccess { exists -> logger.debug("[existsByOrderIdAndContainerId] Relationship exists=$exists: orderId=$orderId, containerId=$containerId") }
            .onErrorMap { e ->
                logger.warn("[existsByOrderIdAndContainerId] Error checking relationship existence: orderId=$orderId, containerId=$containerId", e)
                RepositoryException("Error checking relationship existence", e)
            }
    }
}