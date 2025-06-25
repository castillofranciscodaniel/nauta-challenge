package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.OrderContainer
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderContainerEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class OrderContainerRepositoryImpl(
    private val orderContainerDao: OrderContainerDao
) : OrderContainerRepository {
    private val logger = LoggerFactory.getLogger(OrderContainerRepositoryImpl::class.java)

    override suspend fun save(orderId: Long, containerId: Long): OrderContainer {
        logger.info("[save] Attempting to save order-container relationship: orderId=$orderId, containerId=$containerId")

        return try {
            orderContainerDao.save(OrderContainerEntity(orderId = orderId, containerId = containerId))
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.info("[save] Successfully saved order-container relationship: orderId=$orderId, containerId=$containerId") }
                ?: throw ModelNotSavedException("OrderContainer not saved")
        } catch (e: Exception) {
            logger.error(
                "[save] Error while saving order-container relationship: orderId=$orderId, containerId=$containerId",
                e
            )
            throw ModelNotSavedException("OrderContainer not saved: ${e.message}")
        }
    }

    override suspend fun existsByOrderIdAndContainerId(orderId: Long, containerId: Long): Boolean {
        logger.debug("[existsByOrderIdAndContainerId] Checking if relationship exists: orderId=$orderId, containerId=$containerId")

        return try {
            orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId)
                .awaitSingleOrNull()
                ?.also { logger.debug("[existsByOrderIdAndContainerId] Relationship exists=$it: orderId=$orderId, containerId=$containerId") }
                ?: false
        } catch (e: Exception) {
            logger.warn(
                "[existsByOrderIdAndContainerId] Error checking relationship existence: orderId=$orderId, containerId=$containerId",
                e
            )
            throw RepositoryException("Error al verificar relación entre orden y contenedor", e)
        }
    }
}