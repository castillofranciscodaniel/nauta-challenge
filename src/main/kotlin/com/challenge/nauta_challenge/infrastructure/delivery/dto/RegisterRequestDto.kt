package com.challenge.nauta_challenge.infrastructure.delivery.dto

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