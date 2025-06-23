package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.ContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.ContainerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class ContainerRepositoryImpl(
    private val containerDao: ContainerDao
) : ContainerRepository {

    override suspend fun save(container: Container): Container {
        val containerEntity = ContainerEntity.fromModel(container)
        return containerDao.save(containerEntity).awaitSingleOrNull()?.toModel()
            ?: throw Exception("Container not saved")
    }

    override suspend fun findByContainerNumberAndBookingId(containerNumber: String, bookingId: Long): Container? {
        return containerDao.findByContainerNumberAndBookingId(containerNumber, bookingId)
            .awaitSingleOrNull()?.toModel()
    }

    override fun findContainersByPurchaseNumberAndUserId(purchaseNumber: String, userId: Long): Flow<Container> {
        return containerDao.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId)
            .map { it.toModel() }
            .asFlow()
    }

    override fun findAllByUserId(userId: Long): Flow<Container> {
        return containerDao.findAllByUserId(userId)
            .map { it.toModel() }
            .asFlow()
    }
}