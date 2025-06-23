package com.challenge.nauta_challenge.infrastructure.security

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider()
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 3600000L)
    }

    @Test
    fun `generateToken should create valid JWT with correct claims`() {
        // Arrange
        val email = "test@example.com"
        val userId = 1L

        // Act
        val token = jwtTokenProvider.generateToken(email, userId)

        // Assert
        assertNotNull(token)
        assertTrue(jwtTokenProvider.validateToken(token))

        val extractedEmail = jwtTokenProvider.getUserEmailFromToken(token)
        assertEquals(email, extractedEmail)

        val extractedUserId = jwtTokenProvider.getClaimFromToken(token, "userId")
        // Convertir el valor extraído a Long para evitar problemas de tipos
        val extractedUserIdAsLong = when (extractedUserId) {
            is Int -> extractedUserId.toLong()
            is Long -> extractedUserId
            else -> fail("UserId no es del tipo esperado")
        }
        assertEquals(userId, extractedUserIdAsLong)
    }

    @Test
    fun `validateToken should return true for valid token`() {
        // Arrange
        val token = jwtTokenProvider.generateToken("user@example.com", 1L)

        // Act & Assert
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    @Test
    fun `validateToken should return false for invalid token`() {
        // Arrange
        val invalidToken = "invalid.token.string"

        // Act & Assert
        assertFalse(jwtTokenProvider.validateToken(invalidToken))
    }

    @Test
    fun `validateToken should return false for expired token`() {
        // Arrange
        // Crear un token que expire inmediatamente
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", -1000L)
        val expiredToken = jwtTokenProvider.generateToken("user@example.com", 1L)

        // Act & Assert
        assertFalse(jwtTokenProvider.validateToken(expiredToken))
    }

    @Test
    fun `getUserEmailFromToken should extract email correctly`() {
        // Arrange
        val email = "user@test.com"
        val token = jwtTokenProvider.generateToken(email, 1L)

        // Act
        val extractedEmail = jwtTokenProvider.getUserEmailFromToken(token)

        // Assert
        assertEquals(email, extractedEmail)
    }

    @Test
    fun `getClaimFromToken should return null for non-existent claim`() {
        // Arrange
        val token = jwtTokenProvider.generateToken("user@example.com", 1L)

        // Act
        val nonExistentClaim = jwtTokenProvider.getClaimFromToken(token, "nonExistentClaim")

        // Assert
        assertEquals(null, nonExistentClaim)
    }

    private fun fail(message: String): Nothing {
        throw AssertionError(message)
    }
}
