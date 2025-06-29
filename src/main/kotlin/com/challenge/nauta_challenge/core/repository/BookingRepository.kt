package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.core.model.*
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun save(booking: Booking): Booking
    suspend fun findByBookingNumberAndUserId(bookingNumber: String, userId: Long): Booking?
}

interface ContainerRepository {
    suspend fun save(container: Container): Container
    suspend fun findByContainerNumberAndBookingId(containerNumber: String, bookingId: Long): Container?
    fun findContainersByPurchaseNumberAndUserId(purchaseNumber: String, userId: Long): Flow<Container>
    fun findAllByUserId(userId: Long): Flow<Container>
}

interface OrderRepository {
    suspend fun save(order: Order): Order
    suspend fun findByPurchaseNumberAndBookingId(purchaseNumber: String, bookingId: Long): Order?
    fun findOrdersByContainerIdAndUserId(containerId: String, userId: Long): Flow<Order>
    fun findAllByUserId(userId: Long): Flow<Order>
}

interface InvoiceRepository {
    suspend fun save(invoice: Invoice): Invoice
    fun findAllByOrderId(orderId: Long): Flow<Invoice>
    suspend fun findByInvoiceNumberAndOrderId(invoiceNumber: String, orderId: Long): Invoice?
}

interface OrderContainerRepository {
    suspend fun save(orderId: Long, containerId: Long): OrderContainer
    suspend fun existsByOrderIdAndContainerId(orderId: Long, containerId: Long): Boolean
}