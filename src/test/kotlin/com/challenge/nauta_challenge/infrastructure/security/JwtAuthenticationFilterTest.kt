package com.challenge.nauta_challenge.infrastructure.security

import com.challenge.nauta_challenge.config.TestConfig
import com.challenge.nauta_challenge.core.model.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import reactor.test.StepVerifier
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(classes = [TestConfig::class])
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter
    private lateinit var filter: AuthenticationWebFilter
    private lateinit var authConverter: ServerAuthenticationConverter

    @BeforeEach
    fun setUp() {
        jwtAuthenticationFilter = JwtAuthenticationFilter(jwtTokenProvider)
        filter = jwtAuthenticationFilter.apply()
        // Obtener el converter mediante reflexión ya que no es accesible directamente
        authConverter = ReflectionTestUtils.getField(filter, "authenticationConverter") as ServerAuthenticationConverter
    }

    @Test
    fun `should authenticate with valid token`() {
        // Arrange
        val token = "valid-token"
        val email = "user@example.com"
        val userId = 1L

        every { jwtTokenProvider.validateToken(token) } returns true
        every { jwtTokenProvider.getUserEmailFromToken(token) } returns email
        every { jwtTokenProvider.getClaimFromToken(token, "userId") } returns userId

        val request = MockServerHttpRequest
            .get("/api/some-endpoint")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        
        val exchange = MockServerWebExchange.from(request)

        // Act
        val result = authConverter.convert(exchange)

        // Assert
        StepVerifier.create(result)
            .assertNext { auth ->
                val principal = auth.principal as User
                assertEquals(userId, principal.id)
                assertEquals(email, principal.email)
                assertTrue(auth.authorities.contains(SimpleGrantedAuthority("ROLE_USER")))
            }
            .verifyComplete()

        verify { jwtTokenProvider.validateToken(token) }
        verify { jwtTokenProvider.getUserEmailFromToken(token) }
        verify { jwtTokenProvider.getClaimFromToken(token, "userId") }
    }

    @Test
    fun `should not authenticate with invalid token`() {
        // Arrange
        val token = "invalid-token"

        every { jwtTokenProvider.validateToken(token) } returns false

        val request = MockServerHttpRequest
            .get("/api/some-endpoint")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        
        val exchange = MockServerWebExchange.from(request)

        // Act
        val result = authConverter.convert(exchange)

        // Assert
        StepVerifier.create(result)
            .verifyComplete() // Mono.empty() si la autenticación falla

        verify { jwtTokenProvider.validateToken(token) }
    }

    @Test
    fun `should not authenticate without token`() {
        // Arrange
        val request = MockServerHttpRequest
            .get("/api/some-endpoint")
            .build()
        
        val exchange = MockServerWebExchange.from(request)

        // Act
        val result = authConverter.convert(exchange)

        // Assert
        StepVerifier.create(result)
            .verifyComplete() // Mono.empty() si no hay token
    }

    @Test
    fun `should extract user ID correctly from token`() {
        // Arrange
        val token = "valid-token"
        val email = "user@example.com"
        val userId = 1L

        every { jwtTokenProvider.validateToken(token) } returns true
        every { jwtTokenProvider.getUserEmailFromToken(token) } returns email
        every { jwtTokenProvider.getClaimFromToken(token, "userId") } returns userId

        val request = MockServerHttpRequest
            .get("/api/some-endpoint")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .build()
        
        val exchange = MockServerWebExchange.from(request)

        // Act
        val result = authConverter.convert(exchange)

        // Assert
        StepVerifier.create(result)
            .assertNext { auth ->
                val principal = auth.principal as User
                assertEquals(userId, principal.id)
            }
            .verifyComplete()
    }
}
