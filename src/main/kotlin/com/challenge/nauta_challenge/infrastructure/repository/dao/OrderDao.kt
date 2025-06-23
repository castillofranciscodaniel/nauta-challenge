package com.challenge.nauta_challenge.infrastructure.repository.dao

import com.challenge.nauta_challenge.infrastructure.repository.model.OrderEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface OrderDao : R2dbcRepository<OrderEntity, Long> {
    fun findByPurchaseNumberAndBookingId(purchaseNumber: String, bookingId: Long): Mono<OrderEntity>
    fun findAllByBookingId(bookingId: Long): Flux<OrderEntity>

    @Query(
        """
        SELECT o.* 
        FROM orders o
        INNER JOIN order_containers oc ON o.id = oc.order_id
        INNER JOIN containers c ON oc.container_id = c.id
        INNER JOIN bookings b ON c.booking_id = b.id
        WHERE c.container_number = :containerId
        AND b.user_id = :userId
    """
    )
    fun findOrdersByContainerIdAndUserId(containerId: String, userId: Long): Flux<OrderEntity>

    @Query(
        """
        SELECT o.* 
        FROM orders o
        INNER JOIN bookings b ON o.booking_id = b.id
        WHERE b.user_id = :userId
        """
    )
    fun findAllByUserId(userId: Long): Flux<OrderEntity>
}