package com.challenge.nauta_challenge.infrastructure.repository.dao

import com.challenge.nauta_challenge.infrastructure.repository.model.ContainerEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ContainerDao : R2dbcRepository<ContainerEntity, Long> {
    fun findByContainerNumberAndBookingId(containerNumber: String, bookingId: Long): Mono<ContainerEntity>

    @Query("""
        SELECT c.* 
        FROM containers c
        INNER JOIN order_containers oc ON c.id = oc.container_id
        INNER JOIN orders o ON oc.order_id = o.id
        INNER JOIN bookings b ON o.booking_id = b.id
        WHERE o.purchase_number = :purchaseNumber
        AND b.user_id = :userId
    """)
    fun findContainersByPurchaseNumberAndUserId(purchaseNumber: String, userId: Long): Flux<ContainerEntity>

    @Query("""
        SELECT c.* 
        FROM containers c
        INNER JOIN bookings b ON c.booking_id = b.id
        WHERE b.user_id = :userId
    """)
    fun findAllByUserId(userId: Long): Flux<ContainerEntity>
}