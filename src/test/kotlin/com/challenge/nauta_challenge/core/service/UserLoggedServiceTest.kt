package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.UnauthorizedException
import com.challenge.nauta_challenge.core.model.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals

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
    fun devuelveUsuarioActualCuandoEstáAutenticado() {
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

        // Act & Assert
        StepVerifier.create(userLoggedService.getCurrentUserId())
            .assertNext { resultado ->
                assertEquals(userId, resultado.id)
                assertEquals(userEmail, resultado.email)
            }
            .verifyComplete()
    }

    @Test
    fun lanzaExcepcionCuandoNoHayAutenticacion() {
        // Arrange
        every { securityContext.authentication } returns null

        // Act & Assert
        StepVerifier.create(userLoggedService.getCurrentUserId())
            .expectError(UnauthorizedException::class.java)
            .verify()
    }

    @Test
    fun lanzaExcepcionCuandoPrincipalNoEsCustomUserDetails() {
        // Arrange
        every { securityContext.authentication } returns authentication
        every { authentication.principal } returns "not a CustomUserDetails"

        // Act & Assert
        StepVerifier.create(userLoggedService.getCurrentUserId())
            .expectError(UnauthorizedException::class.java)
            .verify()
    }
}