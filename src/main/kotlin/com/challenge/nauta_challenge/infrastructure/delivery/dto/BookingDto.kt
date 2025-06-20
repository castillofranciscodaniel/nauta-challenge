package com.challenge.nauta_challenge.infrastructure.delivery.dto

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.model.Order

data class BookingDto(
    val booking: String,
    val containers: List<ContainerDto>? = null,
    val orders: List<OrderDto>? = null
) {
    fun toModel() = Booking(
        bookingNumber = this.booking,
        containers = this.containers?.map { it.toModel() } ?: emptyList(),
        orders = this.orders?.map { it.toModel() } ?: emptyList()
    )
}

data class ContainerDto(
    val container: String
) {
    fun toModel() = Container(
        containerNumber = this.container
    )
}

data class OrderDto(
    val purchase: String,
    val invoices: List<InvoiceDto>? = null
) {
    fun toModel() = Order(
        purchaseNumber = this.purchase,
        invoices = this.invoices?.map { it.toModel() } ?: emptyList()
    )
}

data class InvoiceDto(
    val invoice: String
) {
    fun toModel() = Invoice(
        invoiceNumber = this.invoice
    )
}

