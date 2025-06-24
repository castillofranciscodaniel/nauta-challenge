package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.UserRepository
import com.challenge.nauta_challenge.infrastructure.delivery.dto.LoginRequestDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.RegisterRequestDto
import com.challenge.nauta_challenge.infrastructure.security.JwtTokenProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals

@SpringBootTest
class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val authService = AuthService(userRepository, passwordEncoder, jwtTokenProvider)

    @Test
    fun registerUserSuccessfully() {
        // Arrange
        val request = RegisterRequestDto(email = "test@example.com", password = "password")
        val encodedPassword = "encodedPassword"
        val userWithId = User(id = 1, email = "test@example.com", password = encodedPassword)
        val token = "jwt-token"

        every { userRepository.existsByEmail("test@example.com") } returns Mono.just(false)
        every { passwordEncoder.encode("password") } returns encodedPassword
        every { userRepository.save(any()) } returns Mono.just(userWithId)
        every { jwtTokenProvider.generateToken("test@example.com", 1) } returns token

        // Act & Assert
        StepVerifier.create(authService.register(request))
            .assertNext { result ->
                assertEquals("jwt-token", result.token)
                assertEquals("test@example.com", result.email)
            }
            .verifyComplete()
    }

    @Test
    fun throwsExceptionWhenEmailAlreadyExists() {
        // Arrange
        val request = RegisterRequestDto(email = "test@example.com", password = "password")
        every { userRepository.existsByEmail("test@example.com") } returns Mono.just(true)

        // Act & Assert
        StepVerifier.create(authService.register(request))
            .expectErrorMatches { ex ->
                ex is IllegalArgumentException && ex.message == "Email is already registered"
            }
            .verify()
    }

    @Test
    fun loginSuccessfully() {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        val user = User(id = 1, email = "test@example.com", password = "encodedPassword")
        val token = "jwt-token"

        every { userRepository.findByEmail("test@example.com") } returns Mono.just(user)
        every { passwordEncoder.matches("password", "encodedPassword") } returns true
        every { jwtTokenProvider.generateToken("test@example.com", 1) } returns token

        // Act & Assert
        StepVerifier.create(authService.login(request))
            .assertNext { result ->
                assertEquals("jwt-token", result.token)
                assertEquals("test@example.com", result.email)
            }
            .verifyComplete()
    }

    @Test
    fun throwsExceptionWhenUserDoesNotExist() {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        every { userRepository.findByEmail("test@example.com") } returns Mono.empty()

        // Act & Assert
        StepVerifier.create(authService.login(request))
            .expectError(BadCredentialsException::class.java)
            .verify()
    }

    @Test
    fun throwsExceptionWhenPasswordIsIncorrect() {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        val user = User(id = 1, email = "test@example.com", password = "encodedPassword")

        every { userRepository.findByEmail("test@example.com") } returns Mono.just(user)
        every { passwordEncoder.matches("password", "encodedPassword") } returns false

        // Act & Assert
        StepVerifier.create(authService.login(request))
            .expectError(BadCredentialsException::class.java)
            .verify()
    }
}