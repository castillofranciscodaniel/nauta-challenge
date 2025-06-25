package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.service.AuthService
import com.challenge.nauta_challenge.infrastructure.delivery.AuthController
import com.challenge.nauta_challenge.infrastructure.delivery.dto.AuthResponseDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.LoginRequestDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.RegisterRequestDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class AuthControllerTest {

    private val authService = mockk<AuthService>()
    private val authController = AuthController(authService)

    @Test
    fun registroDevuelveRespuestaCorrectaCuandoExitoso(): Unit = runBlocking {
        // Arrange
        val request = RegisterRequestDto(
            email = "juan@example.com",
            password = "password123"
        )

        val expectedResponse = AuthResponseDto(
            token = "jwt-token-123",
            email = "juan@example.com"
        )

        coEvery { authService.register(request) } returns expectedResponse

        // Act
        val result = authController.register(request)

        // Assert
        assertEquals(200, result.statusCodeValue)
        assertEquals(expectedResponse, result.body)
    }

    @Test
    fun loginDevuelveRespuestaCorrectaCuandoExitoso(): Unit = runBlocking {
        // Arrange
        val request = LoginRequestDto(
            email = "juan@example.com",
            password = "password123"
        )

        val expectedResponse = AuthResponseDto(
            token = "jwt-token-123",
            email = "juan@example.com"
        )

        coEvery { authService.login(request) } returns expectedResponse

        // Act
        val result = authController.login(request)

        // Assert
        assertEquals(200, result.statusCodeValue)
        assertEquals(expectedResponse, result.body)
    }
}