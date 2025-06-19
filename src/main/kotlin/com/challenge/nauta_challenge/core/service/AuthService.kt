package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.UserRepository
import com.challenge.nauta_challenge.infrastructure.delivery.dto.AuthResponseDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.LoginRequestDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.RegisterRequestDto
import com.challenge.nauta_challenge.infrastructure.security.JwtTokenProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    suspend fun register(request: RegisterRequestDto): AuthResponseDto {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        val usuario = User(
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        val savedUsuario = userRepository.save(usuario)
        val token = jwtTokenProvider.generateToken(savedUsuario.email)

        return AuthResponseDto(
            token = token,
            email = savedUsuario.email
        )
    }

    suspend fun login(request: LoginRequestDto): AuthResponseDto {
        val usuario = userRepository.findByEmail(request.email)
            ?: throw BadCredentialsException("Email o contraseña incorrectos")

        if (!passwordEncoder.matches(request.password, usuario.password)) {
            throw BadCredentialsException("Email o contraseña incorrectos")
        }

        val token = jwtTokenProvider.generateToken(usuario.email)

        return AuthResponseDto(
            token = token,
            email = usuario.email
        )
    }
}