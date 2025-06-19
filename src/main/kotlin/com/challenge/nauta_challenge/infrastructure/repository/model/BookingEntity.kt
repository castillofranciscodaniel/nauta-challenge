package com.challenge.nauta_challenge.infrastructure.repository.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("bookings")
data class BookingEntity(
    @Id
    val id: Long? = null,
    @Column("booking_number") val bookingNumber: String,
    @Column("user_id") val userId: Long
)

@Table("containers")
data class ContainerEntity(
    @Id
    val id: Long? = null,
    @Column("container_number") val containerNumber: String,
    @Column("booking_id") val bookingId: Long
)

@Table("orders")
data class OrderEntity(
    @Id
    val id: Long? = null,
    @Column("purchase_number") val purchaseNumber: String,
    @Column("booking_id") val bookingId: Long
)

@Table("invoices")
data class InvoiceEntity(
    @Id
    val id: Long? = null,
    @Column("invoice_number") val invoiceNumber: String,
    @Column("order_id") val orderId: Long
)