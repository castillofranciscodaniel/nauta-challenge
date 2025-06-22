package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.UnauthorizedException
import com.challenge.nauta_challenge.core.model.User
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserLoggedService() {

    suspend fun getCurrentUserId(): User {
        val authentication = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()?.authentication
            ?: throw UnauthorizedException("Usuario no autenticado")

        val userLogged = (authentication.principal as CustomUserDetails)

        return User(
            id = userLogged.id,
            email = userLogged.userName
        )

    }
}