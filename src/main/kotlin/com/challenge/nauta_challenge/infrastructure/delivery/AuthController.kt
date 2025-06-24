package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.service.AuthService
import com.challenge.nauta_challenge.infrastructure.delivery.dto.AuthResponseDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.LoginRequestDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.RegisterRequestDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/register")
    suspend fun register(@RequestBody request: RegisterRequestDto): ResponseEntity<AuthResponseDto> {
        logger.info("[register] Registration request received for user: ${request.email}")
        val response = authService.register(request)
        logger.info("[register] Registration successful for user: ${request.email}")
        return ResponseEntity.ok(response)
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequestDto): ResponseEntity<AuthResponseDto> {
        logger.info("[login] Login request received for user: ${request.email}")
        val response = authService.login(request)
        logger.info("[login] Login successful for user: ${request.email}")
        return ResponseEntity.ok(response)
    }
}