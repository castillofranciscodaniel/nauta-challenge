package com.challenge.nauta_challenge.core.model

data class Booking(
    val id: Long? = null,
    val bookingNumber: String,
    val userId: Long? = null,
    val containers: List<Container> = emptyList(),
    val orders: List<Order> = emptyList()
)

data class Container(
    val id: Long? = null,
    val containerNumber: String,
    val bookingId: Long? = null
)

data class Order(
    val id: Long? = null,
    val purchaseNumber: String,
    val invoices: List<Invoice> = emptyList(),
    val bookingId: Long? = null
)

data class Invoice(
    val id: Long? = null,
    val invoiceNumber: String,
    val orderId: Long? = null
)

data class OrderContainer(
    val id: Long? = null,
    val orderId: Long,
    val containerId: Long
)