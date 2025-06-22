package com.challenge.nauta_challenge.infrastructure.security

import com.challenge.nauta_challenge.core.model.User
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtTokenProvider
) {

    fun apply(): AuthenticationWebFilter {
        val authManager = ReactiveAuthenticationManager { auth ->
            // Este auth ya viene validado por el filtro
            Mono.just(auth)
        }

        val filter = AuthenticationWebFilter(authManager)

        filter.setServerAuthenticationConverter { exchange ->
            val token = extractToken(exchange.request)
            if (token != null && jwtService.validateToken(token)) {
                val mail = jwtService.getUserEmailFromToken(token)
                val auth = UsernamePasswordAuthenticationToken(
                    User(
                        id = 1,
                        email = mail
                    ),
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER"))
                )
                Mono.just(auth)
            } else {
                Mono.empty()
            }
        }

        return filter
    }
    private fun extractToken(request: ServerHttpRequest): String? {
        return request.headers.getFirst("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")
    }
}