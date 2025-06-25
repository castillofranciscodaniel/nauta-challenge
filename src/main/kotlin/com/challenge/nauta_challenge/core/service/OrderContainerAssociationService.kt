package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OrderContainerAssociationService(private val orderContainerRepository: OrderContainerRepository) {
    private val logger = LoggerFactory.getLogger(OrderContainerAssociationService::class.java)

    suspend fun createAssociations(orders: List<Order>, containers: List<Container>, bookingNumber: String) {
        logger.info("[createAssociations] Creando asociaciones para booking: {}, órdenes: {}, contenedores: {}",
            bookingNumber, orders.size, containers.size)

        try {
            when {
                orders.size == 1 && containers.isNotEmpty() -> associateOneOrderToManyContainers(orders.first(), containers)
                containers.size == 1 && orders.isNotEmpty() -> associateManyOrdersToOneContainer(orders, containers.first())
                else -> logger.warn("[createAssociations] Relación ambigua en booking {}. No se crearon asociaciones automáticas.", bookingNumber)
            }
            logger.info("[createAssociations] Proceso de asociaciones completado para booking: {}", bookingNumber)
        } catch (e: Exception) {
            logger.error("[createAssociations] Error al crear asociaciones para booking: {}", bookingNumber, e)
            throw e
        }
    }

    private suspend fun associateOneOrderToManyContainers(order: Order, containers: List<Container>) {
        logger.debug("[associateOneOrderToManyContainers] Asociando orden ID: {} con {} contenedores", order.id, containers.size)

        containers.forEach { container ->
            processAssociation(order.id!!, container.id!!)
        }
    }

    private suspend fun associateManyOrdersToOneContainer(orders: List<Order>, container: Container) {
        logger.debug("[associateManyOrdersToOneContainer] Asociando {} órdenes con contenedor ID: {}", orders.size, container.id)

        orders.forEach { order ->
            processAssociation(order.id!!, container.id!!)
        }
    }

    private suspend fun processAssociation(orderId: Long, containerId: Long) {
        try {
            logger.debug("[processAssociation] Verificando asociación entre orden ID: {} y contenedor ID: {}", orderId, containerId)

            if (!orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId)) {
                logger.debug("[processAssociation] Creando nueva asociación")
                orderContainerRepository.save(orderId, containerId).also {
                    logger.debug("[processAssociation] Asociación creada con ID: {}", it.id)
                }
            } else {
                logger.debug("[processAssociation] Asociación ya existe entre orden ID: {} y contenedor ID: {}", orderId, containerId)
            }
        } catch (e: Exception) {
            logger.error("[processAssociation] Error al procesar asociación entre orden ID: {} y contenedor ID: {}", orderId, containerId, e)
            throw e
        }
    }
}