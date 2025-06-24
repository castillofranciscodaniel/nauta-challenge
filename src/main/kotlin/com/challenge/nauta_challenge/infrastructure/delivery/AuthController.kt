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
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequestDto): Mono<ResponseEntity<AuthResponseDto>> {
        logger.info("[register] Registration request received for user: ${request.email}")

        return authService.register(request)
            .map { response ->
                logger.info("[register] Registration successful for user: ${request.email}")
                ResponseEntity.ok(response)
            }
            .doOnError { error ->
                logger.error("[register] Registration failed for user: ${request.email}", error)
            }
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequestDto): Mono<ResponseEntity<AuthResponseDto>> {
        logger.info("[login] Login request received for user: ${request.email}")

        return authService.login(request)
            .map { response ->
                logger.info("[login] Login successful for user: ${request.email}")
                ResponseEntity.ok(response)
            }
            .doOnError { error ->
                logger.error("[login] Login failed for user: ${request.email}", error)
            }
    }
}