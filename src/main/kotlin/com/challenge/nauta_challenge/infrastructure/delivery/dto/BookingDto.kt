package com.challenge.nauta_challenge.infrastructure.delivery.dto

data class BookingDto(
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