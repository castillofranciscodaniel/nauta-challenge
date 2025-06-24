package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.service.AuthService
import com.challenge.nauta_challenge.infrastructure.delivery.dto.AuthResponseDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.LoginRequestDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.RegisterRequestDto
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals

@SpringBootTest
class AuthControllerTest {

    private val authService = mockk<AuthService>()
    private val authController = AuthController(authService)

    @Test
    fun registroDevuelveRespuestaCorrectaCuandoExitoso() {
        // Arrange
        val request = RegisterRequestDto(
            email = "juan@example.com",
            password = "password123"
        )

        val expectedResponse = AuthResponseDto(
            token = "jwt-token-123",
            email = "juan@example.com"
        )

        every { authService.register(request) } returns Mono.just(expectedResponse)

        // Act & Assert
        StepVerifier.create(authController.register(request))
            .assertNext { response ->
                assertEquals(200, response.statusCodeValue)
                assertEquals(expectedResponse, response.body)
            }
            .verifyComplete()
    }

    @Test
    fun loginDevuelveRespuestaCorrectaCuandoExitoso() {
        // Arrange
        val request = LoginRequestDto(
            email = "juan@example.com",
            password = "password123"
        )

        val expectedResponse = AuthResponseDto(
            token = "jwt-token-123",
            email = "juan@example.com"
        )

        every { authService.login(request) } returns Mono.just(expectedResponse)

        // Act & Assert
        StepVerifier.create(authController.login(request))
            .assertNext { response ->
                assertEquals(200, response.statusCodeValue)
                assertEquals(expectedResponse, response.body)
            }
            .verifyComplete()
    }
}