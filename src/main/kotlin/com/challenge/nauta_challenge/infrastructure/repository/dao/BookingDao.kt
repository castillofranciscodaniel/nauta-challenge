package com.challenge.nauta_challenge.infrastructure.repository.dao

import com.challenge.nauta_challenge.infrastructure.repository.model.*
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface BookingDao : R2dbcRepository<BookingEntity, Long> {
    fun findByBookingNumberAndUserId(bookingNumber: String, userId: Long): Mono<BookingEntity>
    fun findAllByUserId(userId: Long): Flux<BookingEntity>
}

@Repository
interface ContainerDao : R2dbcRepository<ContainerEntity, Long> {
    fun findByContainerNumberAndBookingId(containerNumber: String, bookingId: Long): Mono<ContainerEntity>
    fun findAllByBookingIdIn(bookingIds: List<Long>): Flux<ContainerEntity>
    fun findAllByBookingId(bookingId: Long): Flux<ContainerEntity>

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
}

@Repository
interface OrderDao : R2dbcRepository<OrderEntity, Long> {
    fun findByPurchaseNumberAndBookingId(purchaseNumber: String, bookingId: Long): Mono<OrderEntity>
    fun findAllByBookingId(bookingId: Long): Flux<OrderEntity>
}

@Repository
interface InvoiceDao : R2dbcRepository<InvoiceEntity, Long> {
    fun findAllByOrderId(orderId: Long): Flux<InvoiceEntity>
    fun findByInvoiceNumberAndOrderId(invoiceNumber: String, orderId: Long): Mono<InvoiceEntity>
}

@Repository
interface OrderContainerDao : R2dbcRepository<OrderContainerEntity, Long> {
    fun existsByOrderIdAndContainerId(orderId: Long, containerId: Long): Mono<Boolean>
    fun findAllByOrderId(orderId: Long): Flux<OrderContainerEntity>
}