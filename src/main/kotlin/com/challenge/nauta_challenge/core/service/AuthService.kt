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

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    suspend fun register(request: RegisterRequestDto): AuthResponseDto {
        logger.info("[register] Intento de registro para email: {}", request.email)

        try {
            // Verificar si el email ya está registrado
            if (userRepository.existsByEmail(request.email)) {
                logger.warn("[register] Intento de registro con email ya existente: {}", request.email)
                throw IllegalArgumentException("Email ya está registrado")
            }

            logger.debug("[register] Email disponible, creando usuario")

            // Crear y guardar el nuevo usuario
            val user = User(
                email = request.email,
                password = passwordEncoder.encode(request.password)
            )

            val savedUser = userRepository.save(user)
            logger.debug("[register] Usuario guardado con ID: {}", savedUser.id)

            // Generar token
            logger.debug("[register] Generando token para usuario ID: {}", savedUser.id)
            val token = jwtTokenProvider.generateToken(savedUser.email, savedUser.id!!)

            logger.info("[register] Usuario registrado correctamente, email: {}", savedUser.email)
            return AuthResponseDto(
                token = token,
                email = savedUser.email
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> {
                    // Ya tiene log y mensaje apropiado
                    throw e
                }
                else -> {
                    logger.error("[register] Error durante el registro para email: {}", request.email, e)
                    throw e
                }
            }
        }
    }

    suspend fun login(request: LoginRequestDto): AuthResponseDto {
        logger.info("[login] Intento de inicio de sesión para email: {}", request.email)

        try {
            // Buscar usuario por email
            val user = userRepository.findByEmail(request.email)
                ?: run {
                    logger.warn("[login] Usuario no encontrado para email: {}", request.email)
                    throw BadCredentialsException("Email o contraseña incorrectos")
                }

            // Verificar contraseña
            if (!passwordEncoder.matches(request.password, user.password)) {
                logger.warn("[login] Contraseña incorrecta para usuario: {}", request.email)
                throw BadCredentialsException("Email o contraseña incorrectos")
            }

            // Generar token
            logger.debug("[login] Generando token para usuario ID: {}", user.id)
            val token = jwtTokenProvider.generateToken(user.email, user.id!!)

            logger.info("[login] Inicio de sesión exitoso para usuario: {}", user.email)
            return AuthResponseDto(
                token = token,
                email = user.email
            )
        } catch (e: Exception) {
            when (e) {
                is BadCredentialsException -> {
                    // Ya tiene log y mensaje apropiado
                    throw e
                }
                else -> {
                    logger.error("[login] Error durante el inicio de sesión para email: {}", request.email, e)
                    throw e
                }
            }
        }
    }
}