package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.UserRepository
import com.challenge.nauta_challenge.infrastructure.delivery.dto.AuthResponseDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.LoginRequestDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.RegisterRequestDto
import com.challenge.nauta_challenge.infrastructure.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun register(request: RegisterRequestDto): Mono<AuthResponseDto> {
        logger.info("[register] Processing registration request for email: ${request.email}")

        return userRepository.existsByEmail(request.email)
            .flatMap { exists ->
                if (exists) {
                    logger.warn("[register] Email already registered: ${request.email}")
                    Mono.error(IllegalArgumentException("Email is already registered"))
                } else {
                    val user = User(
                        email = request.email,
                        password = passwordEncoder.encode(request.password)
                    )
                    logger.debug("[register] Creating new user with email: ${request.email}")
                    userRepository.save(user)
                }
            }
            .map { savedUser ->
                logger.info("[register] User registered successfully: ${savedUser.email}")
                val token = jwtTokenProvider.generateToken(savedUser.email, savedUser.id!!)
                AuthResponseDto(
                    token = token,
                    email = savedUser.email
                )
            }
            .onErrorMap { e ->
                if (e is IllegalArgumentException) e
                else {
                    logger.error("[register] Error during registration for email: ${request.email}", e)
                    RuntimeException("Registration failed: ${e.message}")
                }
            }
    }

    fun login(request: LoginRequestDto): Mono<AuthResponseDto> {
        logger.info("[login] Processing login request for email: ${request.email}")

        return userRepository.findByEmail(request.email)
            .switchIfEmpty {
                logger.warn("[login] User not found for email: ${request.email}")
                Mono.error(BadCredentialsException("Incorrect email or password"))
            }
            .flatMap { user ->
                if (passwordEncoder.matches(request.password, user.password)) {
                    logger.info("[login] Login successful for email: ${request.email}")
                    val token = jwtTokenProvider.generateToken(user.email, user.id!!)
                    Mono.just(AuthResponseDto(
                        token = token,
                        email = user.email
                    ))
                } else {
                    logger.warn("[login] Invalid password for email: ${request.email}")
                    Mono.error(BadCredentialsException("Incorrect email or password"))
                }
            }
            .onErrorMap { e ->
                if (e is BadCredentialsException) e
                else {
                    logger.error("[login] Error during login for email: ${request.email}", e)
                    RuntimeException("Login failed: ${e.message}")
                }
            }
    }
}