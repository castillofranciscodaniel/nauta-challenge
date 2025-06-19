package com.challenge.nauta_challenge.infrastructure.delivery.dto

data class EmailRequestDto(
    val booking: String? = null,
    val containers: List<ContainerDto>? = null,
    val orders: List<OrderDto>? = null
)

data class ContainerDto(
    val container: String
)

data class OrderDto(
    val purchase: String,
    val invoices: List<InvoiceDto>? = null
)

data class InvoiceDto(
    val invoice: String
)

// DTOs de respuesta
data class ContainerResponseDto(
    val id: Long,
    val containerNumber: String,
    val bookings: List<String>? = null,
    val orders: List<String>? = null
)

data class OrderResponseDto(
    val id: Long,
    val purchaseNumber: String,
    val invoices: List<String>? = null,
    val bookings: List<String>? = null,
    val containers: List<String>? = null
)

data class RegisterRequestDto(
    val email: String,
    val password: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val token: String,
    val email: String
)