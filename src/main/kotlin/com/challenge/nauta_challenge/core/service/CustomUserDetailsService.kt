package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val usuario = runBlocking { userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("Usuario no encontrado con el email: $email") }

        return User
            .withUsername(email)
            .password(usuario.password)
            .authorities(SimpleGrantedAuthority("ROLE_USER"))
            .build()
    }
}