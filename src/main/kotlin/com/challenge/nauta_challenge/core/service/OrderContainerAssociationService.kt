package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import org.springframework.stereotype.Service

// OrderContainerAssociationService.kt
@Service
class OrderContainerAssociationService(private val orderContainerRepository: OrderContainerRepository) {
    suspend fun createAssociations(orders: List<Order>, containers: List<Container>, bookingNumber: String) {
        when {
            orders.size == 1 && containers.isNotEmpty() ->
                associateOneOrderToManyContainers(orders.first(), containers)

            containers.size == 1 && orders.isNotEmpty() ->
                associateManyOrdersToOneContainer(orders, containers.first())

            else -> println("Ambiguous relationship in booking $bookingNumber. No automatic associations created.")
        }
    }

    private suspend fun associateOneOrderToManyContainers(order: Order, containers: List<Container>) {
        containers.forEach { container ->
            if (!orderContainerRepository.existsByOrderIdAndContainerId(order.id!!, container.id!!)) {
                orderContainerRepository.save(order.id, container.id)
            }
        }
    }

    private suspend fun associateManyOrdersToOneContainer(orders: List<Order>, container: Container) {
        orders.forEach { order ->
            if (!orderContainerRepository.existsByOrderIdAndContainerId(order.id!!, container.id!!)) {
                orderContainerRepository.save(order.id, container.id)
            }
        }
    }
}