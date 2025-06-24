package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.UserRepository
import com.challenge.nauta_challenge.infrastructure.delivery.dto.LoginRequestDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.RegisterRequestDto
import com.challenge.nauta_challenge.infrastructure.security.JwtTokenProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val authService = AuthService(userRepository, passwordEncoder, jwtTokenProvider)

    @Test
    fun registerUserSuccessfully() = runTest {
        // Arrange
        val request = RegisterRequestDto(email = "test@example.com", password = "password")
        val encodedPassword = "encodedPassword"
        val userWithId = User(id = 1, email = "test@example.com", password = encodedPassword)
        val token = "jwt-token"

        coEvery { userRepository.existsByEmail("test@example.com") } returns false
        every { passwordEncoder.encode("password") } returns encodedPassword
        coEvery { userRepository.save(any()) } returns userWithId
        every { jwtTokenProvider.generateToken("test@example.com", 1) } returns token

        // Act
        val result = authService.register(request)

        // Assert
        assertEquals("jwt-token", result.token)
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun throwsExceptionWhenEmailAlreadyExists(): Unit = runTest {
        // Arrange
        val request = RegisterRequestDto(email = "test@example.com", password = "password")
        coEvery { userRepository.existsByEmail("test@example.com") } returns true

        // Act & Assert
        assertFailsWith<IllegalArgumentException>("Email is already registered") {
            authService.register(request)
        }
    }

    @Test
    fun loginSuccessfully() = runTest {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        val user = User(id = 1, email = "test@example.com", password = "encodedPassword")
        val token = "jwt-token"

        coEvery { userRepository.findByEmail("test@example.com") } returns user
        every { passwordEncoder.matches("password", "encodedPassword") } returns true
        every { jwtTokenProvider.generateToken("test@example.com", 1) } returns token

        // Act
        val result = authService.login(request)

        // Assert
        assertEquals("jwt-token", result.token)
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun throwsExceptionWhenUserDoesNotExist(): Unit = runTest {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        coEvery { userRepository.findByEmail("test@example.com") } returns null

        // Act & Assert
        assertFailsWith<BadCredentialsException>("Incorrect email or password") {
            authService.login(request)
        }
    }

    @Test
    fun throwsExceptionWhenPasswordIsIncorrect(): Unit = runTest {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        val user = User(id = 1, email = "test@example.com", password = "encodedPassword")

        coEvery { userRepository.findByEmail("test@example.com") } returns user
        every { passwordEncoder.matches("password", "encodedPassword") } returns false

        // Act & Assert
        assertFailsWith<BadCredentialsException>("Incorrect email or password") {
            authService.login(request)
        }
    }
}