package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.ContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.ContainerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ContainerRepositoryImpl(
    private val containerDao: ContainerDao
) : ContainerRepository {
    private val logger = LoggerFactory.getLogger(ContainerRepositoryImpl::class.java)

    override suspend fun save(container: Container): Container {
        logger.info("[save] Attempting to save container: containerNumber=${container.containerNumber}, bookingId=${container.bookingId}")

        return try {
            val containerEntity = ContainerEntity.fromModel(container)
            containerDao.save(containerEntity)
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.info("[save] Successfully saved container: id=${it.id}, containerNumber=${it.containerNumber}") }
                ?: throw ModelNotSavedException("Container not saved")
        } catch (e: Exception) {
            logger.error("[save] Error while saving container: containerNumber=${container.containerNumber}", e)
            throw ModelNotSavedException("Container not saved: ${e.message}")
        }
    }

    override suspend fun findByContainerNumberAndBookingId(containerNumber: String, bookingId: Long): Container? {
        logger.debug("[findByContainerNumberAndBookingId] Looking for container: containerNumber=$containerNumber, bookingId=$bookingId")

        return try {
            containerDao.findByContainerNumberAndBookingId(containerNumber, bookingId)
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.debug("[findByContainerNumberAndBookingId] Found container: id=${it.id}") }
                ?: run {
                    logger.debug("[findByContainerNumberAndBookingId] Container not found: containerNumber=$containerNumber, bookingId=$bookingId")
                    null
                }
        } catch (e: Exception) {
            logger.warn(
                "[findByContainerNumberAndBookingId] Error looking for container: containerNumber=$containerNumber, bookingId=$bookingId",
                e
            )
            throw RepositoryException("Error finding container", e)
        }
    }

    override fun findContainersByPurchaseNumberAndUserId(purchaseNumber: String, userId: Long): Flow<Container> {
        logger.debug("[findContainersByPurchaseNumberAndUserId] Looking for containers by purchase: purchaseNumber=$purchaseNumber, userId=$userId")
        return try {
            containerDao.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId)
                .map { it.toModel() }
                .asFlow()
                .onStart { logger.debug("[findContainersByPurchaseNumberAndUserId] Starting container search: purchaseNumber=$purchaseNumber") }
                .onCompletion { error ->
                    if (error == null) {
                        logger.debug("[findContainersByPurchaseNumberAndUserId] Completed container search: purchaseNumber=$purchaseNumber")
                    }
                }
        } catch (e: Exception) {
            logger.error(
                "[findContainersByPurchaseNumberAndUserId] Error retrieving containers by purchase: purchaseNumber=$purchaseNumber",
                e
            )
            flow { throw RepositoryException("Error retrieving containers by purchase", e) }
        }
    }

    override fun findAllByUserId(userId: Long): Flow<Container> {
        logger.debug("[findAllByUserId] Looking for all containers for userId=$userId")

        return try {
            containerDao.findAllByUserId(userId)
                .map { it.toModel() }
                .asFlow()
                .onStart { logger.debug("[findAllByUserId] Starting container search for userId=$userId") }
                .onCompletion { error ->
                    if (error == null) {
                        logger.debug("[findAllByUserId] Completed container search for userId=$userId")
                    }
                }
        } catch (e: Exception) {
            logger.error("[findAllByUserId] Error while retrieving containers for userId=$userId", e)
            flow { throw RepositoryException("Error retrieving containers for user: ${e.message}", e) }
        }
    }
}