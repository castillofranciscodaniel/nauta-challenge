package com.challenge.nauta_challenge.core.model

data class Booking(
    val id: Long? = null,
    val bookingNumber: String,
    val userId: Long,
    val containers: List<Container> = emptyList()
)

data class Container(
    val id: Long? = null,
    val containerNumber: String
)

data class Order(
    val id: Long? = null,
    val purchaseNumber: String,
    val invoices: List<Invoice> = emptyList()
)

data class Invoice(
    val id: Long? = null,
    val invoiceNumber: String
)