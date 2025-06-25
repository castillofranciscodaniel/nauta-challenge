package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.UnauthorizedException
import com.challenge.nauta_challenge.core.model.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class UserLoggedServiceTest {

    private val userLoggedService = UserLoggedService()
    private val securityContext = mockk<SecurityContext>()
    private val authentication = mockk<Authentication>()

    @BeforeEach
    fun setup() {
        mockkStatic(ReactiveSecurityContextHolder::class)
        every { ReactiveSecurityContextHolder.getContext() } returns Mono.just(securityContext)
    }

    @AfterEach
    fun tearDown() {
        // Limpiar mocks estáticos
    }

    @Test
    fun devuelveUsuarioActualCuandoEstáAutenticado(): Unit = runTest {
        // Arrange
        val userId = 1L
        val userEmail = "test@example.com"
        val user = User(
            userId,
            userEmail,
            "password"
        )

        every { securityContext.authentication } returns authentication
        every { authentication.principal } returns user

        // Act
        val resultado = userLoggedService.getCurrentUser()

        // Assert
        assertEquals(userId, resultado.id)
        assertEquals(userEmail, resultado.email)
    }

    @Test
    fun `lanza UnauthorizedException cuando no hay autenticación`() = runTest {
        // Arrange
        every { securityContext.authentication } returns null

        // Act & Assert
        val exception = assertFailsWith<UnauthorizedException> {
            userLoggedService.getCurrentUser()
        }

        assertEquals("Usuario no autenticado", exception.message)
    }

    @Test
    fun `lanza UnauthorizedException cuando ReactiveSecurityContextHolder devuelve mono vacío`() = runTest {
        // Arrange
        every { ReactiveSecurityContextHolder.getContext() } returns Mono.empty()

        // Act & Assert
        val exception = assertFailsWith<UnauthorizedException> {
            userLoggedService.getCurrentUser()
        }

        assertEquals("Usuario no autenticado", exception.message)
    }

    @Test
    fun `lanza UnauthorizedException cuando ocurre un error al obtener el principal`() = runTest {
        // Arrange
        every { securityContext.authentication } returns authentication
        every { authentication.principal } throws RuntimeException("Error al acceder al principal")

        // Act & Assert
        val exception = assertFailsWith<UnauthorizedException> {
            userLoggedService.getCurrentUser()
        }

        assertEquals("Error al obtener el usuario: Error al acceder al principal", exception.message)
    }

    @Test
    fun `lanza UnauthorizedException cuando el principal no es del tipo User`() = runTest {
        // Arrange
        every { securityContext.authentication } returns authentication
        every { authentication.principal } returns null

        // Act & Assert
        assertFailsWith<UnauthorizedException> {
            userLoggedService.getCurrentUser()
        }
    }

}