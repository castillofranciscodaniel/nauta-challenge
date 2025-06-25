package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.service.AuthService
import com.challenge.nauta_challenge.infrastructure.delivery.dto.AuthResponseDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.LoginRequestDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.RegisterRequestDto
import org.springframework.http.HttpStatus
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

        return try {
            val response = authService.register(request)
            logger.info("[register] Registration successful for user: ${request.email}")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("[register] Error during registration for user: ${request.email}", e)
            throw e
        }
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequestDto): ResponseEntity<Any> {
        logger.info("[login] Login request received for user: ${request.email}")

        return try {
            val response = authService.login(request)
            logger.info("[login] Login successful for user: ${request.email}")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("[login] Error during login for user: ${request.email}", e)
            ResponseEntity(
                mapOf("error" to "Error en el inicio de sesión: ${e.message}"),
                HttpStatus.UNAUTHORIZED
            )
        }
    }
}