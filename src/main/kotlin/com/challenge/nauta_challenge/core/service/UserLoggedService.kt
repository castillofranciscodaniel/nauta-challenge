package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.UnauthorizedException
import com.challenge.nauta_challenge.core.model.User
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserLoggedService() {
    private val logger = LoggerFactory.getLogger(UserLoggedService::class.java)

    suspend fun getCurrentUser(): User {
        logger.debug("[getCurrentUser] Obteniendo usuario actual del contexto de seguridad")

        try {
            val authentication = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()?.authentication
                ?: run {
                    logger.warn("[getCurrentUser] No se encontró autenticación en el contexto de seguridad")
                    throw UnauthorizedException("Usuario no autenticado")
                }

            val user = (authentication.principal as User)
            logger.debug("[getCurrentUser] Usuario recuperado correctamente, ID: {}", user.id)
            return user
        } catch (e: UnauthorizedException) {
            // Re-lanzamos la excepción sin envolverla, ya que es específica
            logger.error("[getCurrentUser] Error de autorización: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("[getCurrentUser] Error al obtener el usuario actual", e)
            throw UnauthorizedException("Error al obtener el usuario: ${e.message}")
        }
    }
}