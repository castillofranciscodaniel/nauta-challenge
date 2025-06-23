package com.challenge.nauta_challenge.infrastructure.repository.dao

import com.challenge.nauta_challenge.infrastructure.repository.model.OrderContainerEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface OrderContainerDao : R2dbcRepository<OrderContainerEntity, Long> {
    fun existsByOrderIdAndContainerId(orderId: Long, containerId: Long): Mono<Boolean>
    fun findAllByOrderId(orderId: Long): Flux<OrderContainerEntity>
    fun findAllByContainerId(containerId: Long): Flux<OrderContainerEntity>
}