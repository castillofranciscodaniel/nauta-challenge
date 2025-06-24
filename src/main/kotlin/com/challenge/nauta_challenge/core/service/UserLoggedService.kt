package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.UnauthorizedException
import com.challenge.nauta_challenge.core.model.User
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserLoggedService {
    private val logger = LoggerFactory.getLogger(UserLoggedService::class.java)

    fun getCurrentUserId(): Mono<User> {
        logger.debug("[getCurrentUserId] Retrieving current authenticated user")

        return ReactiveSecurityContextHolder.getContext()
            .map { securityContext ->
                securityContext.authentication?.principal as? User
                    ?: throw UnauthorizedException("User not authenticated")
            }
            .doOnSuccess { user -> logger.debug("[getCurrentUserId] Found authenticated user: id=${user.id}, email=${user.email}") }
            .onErrorMap { e ->
                if (e is UnauthorizedException) {
                    logger.warn("[getCurrentUserId] Unauthorized access attempt", e)
                    e
                } else {
                    logger.error("[getCurrentUserId] Error retrieving current user", e)
                    UnauthorizedException("Error retrieving authenticated user: ${e.message}")
                }
            }
    }
}