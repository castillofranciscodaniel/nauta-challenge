package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.core.model.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface BookingRepository {
    fun save(booking: Booking): Mono<Booking>
    fun findByBookingNumberAndUserId(bookingNumber: String, userId: Long): Mono<Booking>
}

interface ContainerRepository {
    fun save(container: Container): Mono<Container>
    fun findByContainerNumberAndBookingId(containerNumber: String, bookingId: Long): Mono<Container>
    fun findContainersByPurchaseNumberAndUserId(purchaseNumber: String, userId: Long): Flux<Container>
    fun findAllByUserId(userId: Long): Flux<Container>
}

interface OrderRepository {
    fun save(order: Order): Mono<Order>
    fun findByPurchaseNumberAndBookingId(purchaseNumber: String, bookingId: Long): Mono<Order>
    fun findOrdersByContainerIdAndUserId(containerId: String, userId: Long): Flux<Order>
    fun findAllByUserId(userId: Long): Flux<Order>
}

interface InvoiceRepository {
    fun save(invoice: Invoice): Mono<Invoice>
    fun findAllByOrderId(orderId: Long): Flux<Invoice>
    fun findByInvoiceNumberAndOrderId(invoiceNumber: String, orderId: Long): Mono<Invoice>
}

interface OrderContainerRepository {
    fun save(orderId: Long, containerId: Long): Mono<OrderContainer>
    fun existsByOrderIdAndContainerId(orderId: Long, containerId: Long): Mono<Boolean>
}

