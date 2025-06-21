package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.model.Order

interface BookingRepository {
    suspend fun save(booking: Booking): Booking
    suspend fun findByBookingNumberAndUserId(bookingNumber: String, userId: Long): Booking?
}

interface ContainerRepository {
    suspend fun save(container: Container): Container
}

interface OrderRepository {
    suspend fun save(order: Order): Order
}

interface InvoiceRepository {
    suspend fun save(invoice: Invoice): Invoice
}