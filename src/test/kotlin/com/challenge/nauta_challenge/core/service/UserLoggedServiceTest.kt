package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.UnauthorizedException
import com.challenge.nauta_challenge.infrastructure.security.CustomUserDetails
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class UserLoggedServiceTest {

    private val userLoggedService = UserLoggedService()
    private val securityContext = mockk<SecurityContext>()
    private val authentication = mockk<Authentication>()

    @BeforeEach
    fun setup() {
        mockkStatic(SecurityContextHolder::class)
        every { SecurityContextHolder.getContext() } returns securityContext
    }

    @AfterEach
    fun tearDown() {
        // Limpiar mocks estáticos
    }

    @Test
    fun devuelveUsuarioActualCuandoEstáAutenticado(): Unit = runBlocking {
        // Arrange
        val userId = 1L
        val userEmail = "test@example.com"
        val customUserDetails = CustomUserDetails(
            userId,
            userEmail,
            "password",
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )

        every { securityContext.authentication } returns authentication
        every { authentication.principal } returns customUserDetails

        // Act
        val resultado = userLoggedService.getCurrentUserId()

        // Assert
        assertEquals(userId, resultado.id)
        assertEquals(userEmail, resultado.email)
    }

    @Test
    fun lanzaExcepcionCuandoNoHayAutenticacion(): Unit = runBlocking {
        // Arrange
        every { securityContext.authentication } returns null

        // Act & Assert
        assertFailsWith<UnauthorizedException>("Usuario no autenticado") {
            userLoggedService.getCurrentUserId()
        }
    }

    @Test
    fun lanzaExcepcionCuandoPrincipalNoEsCustomUserDetails(): Unit = runBlocking {
        // Arrange
        every { securityContext.authentication } returns authentication
        every { authentication.principal } returns "not a CustomUserDetails"

        // Act & Assert
        assertFailsWith<ClassCastException> {
            userLoggedService.getCurrentUserId()
        }
    }
}