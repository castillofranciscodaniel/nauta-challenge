package com.challenge.nauta_challenge.core.model

data class Booking(
    val id: Long? = null,
    val bookingNumber: String,
    val userId: Long
)

data class Container(
    val id: Long? = null,
    val containerNumber: String,
    val bookingId: Long
)

data class Order(
    val id: Long? = null,
    val purchaseNumber: String,
    val bookingId: Long
)

data class Invoice(
    val id: Long? = null,
    val invoiceNumber: String,
    val orderId: Long
)