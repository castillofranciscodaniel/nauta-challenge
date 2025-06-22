package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.NotFoundException
import com.challenge.nauta_challenge.core.model.OrderContainer
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderContainerEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class OrderContainerRepositoryImpl(
    private val orderContainerDao: OrderContainerDao
) : OrderContainerRepository {

    override suspend fun save(orderId: Long, containerId: Long): OrderContainer {
        val orderContainerEntity = OrderContainerEntity(orderId = orderId, containerId = containerId)
        return orderContainerDao.save(orderContainerEntity).awaitSingleOrNull()?.toModel()
            ?: throw NotFoundException("OrderContainer not saved")
    }

    override suspend fun existsByOrderIdAndContainerId(orderId: Long, containerId: Long): Boolean {
        return orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId).awaitSingleOrNull() ?: false
    }
}