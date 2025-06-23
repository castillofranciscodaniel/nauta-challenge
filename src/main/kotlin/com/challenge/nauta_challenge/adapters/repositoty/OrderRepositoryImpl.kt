package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.OrderRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class OrderRepositoryImpl(
    private val orderDao: OrderDao
) : OrderRepository {
    override suspend fun save(order: Order): Order {
        val orderEntity = OrderEntity.fromModel(order)
        return orderDao.save(orderEntity).awaitSingleOrNull()?.toModel()
            ?: throw Exception("Order not saved")
    }

    override suspend fun findByPurchaseNumberAndBookingId(purchaseNumber: String, bookingId: Long): Order? {
        return orderDao.findByPurchaseNumberAndBookingId(purchaseNumber, bookingId)
            .awaitSingleOrNull()?.toModel()
    }

    override fun findOrdersByContainerIdAndUserId(containerId: String, userId: Long): Flow<Order> {
        return orderDao.findOrdersByContainerIdAndUserId(containerId, userId)
            .map { it.toModel() }
            .asFlow()
    }

    override fun findAllByUserId(userId: Long): Flow<Order> {
        return orderDao.findAllByUserId(userId)
            .map { it.toModel() }
            .asFlow()
    }
}