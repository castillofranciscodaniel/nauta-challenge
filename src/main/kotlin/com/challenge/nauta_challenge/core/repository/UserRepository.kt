package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.core.model.User
import reactor.core.publisher.Mono

interface UserRepository {
    fun findByEmail(email: String): Mono<User>
    fun existsByEmail(email: String): Mono<Boolean>
    fun save(user: User): Mono<User>
}
