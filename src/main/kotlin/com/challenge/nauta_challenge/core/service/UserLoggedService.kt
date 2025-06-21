package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.UnauthorizedException
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.infrastructure.security.CustomUserDetails
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserLoggedService() {

    suspend fun getCurrentUserId(): User {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException("Usuario no autenticado")

        val userLogged = (authentication.principal as CustomUserDetails)

        return User(
            id = userLogged.id,
            email = userLogged.userName
        )

    }
}