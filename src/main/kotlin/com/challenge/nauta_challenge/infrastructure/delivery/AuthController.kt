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

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    suspend fun register(@RequestBody request: RegisterRequestDto): ResponseEntity<AuthResponseDto> {
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequestDto): ResponseEntity<AuthResponseDto> {
        return ResponseEntity.ok(authService.login(request))
    }
}