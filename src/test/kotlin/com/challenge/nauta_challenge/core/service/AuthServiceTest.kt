package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.UserRepository
import com.challenge.nauta_challenge.infrastructure.delivery.dto.LoginRequestDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.RegisterRequestDto
import com.challenge.nauta_challenge.infrastructure.security.JwtTokenProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
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
    fun registraUsuarioCorrectamente() = runBlocking {
        // Arrange
        val request = RegisterRequestDto(email = "test@example.com", password = "password")
        val encodedPassword = "encodedPassword"
        val usuarioConId = User(id = 1, email = "test@example.com", password = encodedPassword)
        val token = "jwt-token"

        coEvery { userRepository.existsByEmail("test@example.com") } returns false
        every { passwordEncoder.encode("password") } returns encodedPassword
        coEvery { userRepository.save(any()) } returns usuarioConId
        every { jwtTokenProvider.generateToken("test@example.com", 1) } returns token

        // Act
        val resultado = authService.register(request)

        // Assert
        assertEquals("jwt-token", resultado.token)
        assertEquals("test@example.com", resultado.email)
    }

    @Test
    fun lanzaExcepcionCuandoEmailYaExiste(): Unit = runBlocking {
        // Arrange
        val request = RegisterRequestDto(email = "test@example.com", password = "password")
        coEvery { userRepository.existsByEmail("test@example.com") } returns true

        // Act & Assert
        assertFailsWith<IllegalArgumentException>("El email ya está registrado") {
            authService.register(request)
        }
    }

    @Test
    fun loginCorrectamente() = runBlocking {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        val usuario = User(id = 1, email = "test@example.com", password = "encodedPassword")
        val token = "jwt-token"

        coEvery { userRepository.findByEmail("test@example.com") } returns usuario
        every { passwordEncoder.matches("password", "encodedPassword") } returns true
        every { jwtTokenProvider.generateToken("test@example.com", 1) } returns token

        // Act
        val resultado = authService.login(request)

        // Assert
        assertEquals("jwt-token", resultado.token)
        assertEquals("test@example.com", resultado.email)
    }

    @Test
    fun lanzaExcepcionCuandoUsuarioNoExiste(): Unit = runBlocking {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        coEvery { userRepository.findByEmail("test@example.com") } returns null

        // Act & Assert
        assertFailsWith<BadCredentialsException>("Email o contraseña incorrectos") {
            authService.login(request)
        }
    }

    @Test
    fun lanzaExcepcionCuandoContraseñaIncorrecta(): Unit = runBlocking {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        val usuario = User(id = 1, email = "test@example.com", password = "encodedPassword")

        coEvery { userRepository.findByEmail("test@example.com") } returns usuario
        every { passwordEncoder.matches("password", "encodedPassword") } returns false

        // Act & Assert
        assertFailsWith<BadCredentialsException>("Email o contraseña incorrectos") {
            authService.login(request)
        }
    }
}