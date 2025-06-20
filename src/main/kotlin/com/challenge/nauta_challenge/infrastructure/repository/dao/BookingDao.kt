package com.challenge.nauta_challenge.infrastructure.repository.dao

import com.challenge.nauta_challenge.infrastructure.repository.model.BookingEntity
import com.challenge.nauta_challenge.infrastructure.repository.model.ContainerEntity
import com.challenge.nauta_challenge.infrastructure.repository.model.InvoiceEntity
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderEntity
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface BookingDao : R2dbcRepository<BookingEntity, Long> {
    fun findByUserId(usuarioId: Long): Flux<BookingEntity>
    fun findByBookingNumberAndUserId(bookingNumber: String, userId: Long): Mono<BookingEntity>
}

@Repository
interface ContainerDao : R2dbcRepository<ContainerEntity, Long> {
    fun findByContainerNumber(containerNumber: String): Mono<ContainerEntity>
}

@Repository
interface OrderDao: R2dbcRepository<OrderEntity, Long> {
    fun findByPurchaseNumber(purchaseNumber: String): Mono<OrderEntity>
}

@Repository
interface InvoiceDao: R2dbcRepository<InvoiceEntity, Long> {
    fun findByInvoiceNumber(invoiceNumber: String): Mono<InvoiceEntity>
}