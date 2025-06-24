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
            throw IllegalArgumentException("Email is already registered")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        val savedUser = userRepository.save(user)
        val token = jwtTokenProvider.generateToken(savedUser.email, savedUser.id!!)

        return AuthResponseDto(
            token = token,
            email = savedUser.email
        )
    }

    suspend fun login(request: LoginRequestDto): AuthResponseDto {
        val user = userRepository.findByEmail(request.email)
            ?: throw BadCredentialsException("Incorrect email or password")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Incorrect email or password")
        }

        val token = jwtTokenProvider.generateToken(user.email, user.id!!)

        return AuthResponseDto(
            token = token,
            email = user.email
        )
    }
}