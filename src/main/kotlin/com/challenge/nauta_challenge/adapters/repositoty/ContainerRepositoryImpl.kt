package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.ContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.ContainerEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorMap
import reactor.kotlin.core.publisher.onErrorResume

@Component
class ContainerRepositoryImpl(
    private val containerDao: ContainerDao
) : ContainerRepository {
    private val logger = LoggerFactory.getLogger(ContainerRepositoryImpl::class.java)

    override fun save(container: Container): Mono<Container> {
        logger.info("[save] Attempting to save container: containerNumber=${container.containerNumber}, bookingId=${container.bookingId}")

        val containerEntity = ContainerEntity.fromModel(container)
        return containerDao.save(containerEntity)
            .map { it.toModel() }
            .doOnSuccess { logger.info("[save] Successfully saved container: id=${it.id}, containerNumber=${it.containerNumber}") }
            .switchIfEmpty(Mono.error(ModelNotSavedException("Container not saved")))
            .onErrorMap { e ->
                logger.error("[save] Error while saving container: containerNumber=${container.containerNumber}", e)
                ModelNotSavedException("Container not saved: ${e.message}")
            }
    }

    override fun findByContainerNumberAndBookingId(containerNumber: String, bookingId: Long): Mono<Container> {
        logger.debug("[findByContainerNumberAndBookingId] Looking for container: containerNumber=$containerNumber, bookingId=$bookingId")

        return containerDao.findByContainerNumberAndBookingId(containerNumber, bookingId)
            .map { it.toModel() }
            .doOnSuccess { container ->
                if (container != null) {
                    logger.debug("[findByContainerNumberAndBookingId] Found container: id=${container.id}")
                } else {
                    logger.debug("[findByContainerNumberAndBookingId] Container not found: containerNumber=$containerNumber, bookingId=$bookingId")
                }
            }
            .onErrorMap { e ->
                logger.warn("[findByContainerNumberAndBookingId] Error looking for container: containerNumber=$containerNumber, bookingId=$bookingId", e)
                RepositoryException("Error finding container", e)
            }
    }

    override fun findContainersByPurchaseNumberAndUserId(purchaseNumber: String, userId: Long): Flux<Container> {
        logger.debug("[findContainersByPurchaseNumberAndUserId] Looking for containers by purchase: purchaseNumber=$purchaseNumber, userId=$userId")

        return containerDao.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId)
            .map { it.toModel() }
            .doOnSubscribe { logger.debug("[findContainersByPurchaseNumberAndUserId] Starting container search: purchaseNumber=$purchaseNumber") }
            .doOnComplete { logger.debug("[findContainersByPurchaseNumberAndUserId] Completed container search: purchaseNumber=$purchaseNumber") }
            .onErrorMap { e ->
                logger.error("[findContainersByPurchaseNumberAndUserId] Error retrieving containers by purchase: purchaseNumber=$purchaseNumber", e)
                RepositoryException("Error retrieving containers by purchase", e)
            }
    }

    override fun findAllByUserId(userId: Long): Flux<Container> {
        logger.debug("[findAllByUserId] Looking for all containers for userId=$userId")

        return containerDao.findAllByUserId(userId)
            .map { it.toModel() }
            .doOnSubscribe { logger.debug("[findAllByUserId] Starting container search for userId=$userId") }
            .doOnComplete { logger.debug("[findAllByUserId] Completed container search for userId=$userId") }
            .onErrorMap { e ->
                logger.error("[findAllByUserId] Error retrieving containers for userId=$userId", e)
                RepositoryException("Error retrieving containers for user", e)
            }
    }
}