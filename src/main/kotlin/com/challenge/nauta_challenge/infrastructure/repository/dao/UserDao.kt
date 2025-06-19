package com.challenge.nauta_challenge.infrastructure.repository.dao

import com.challenge.nauta_challenge.infrastructure.repository.model.UserEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserDao : R2dbcRepository<UserEntity, Long> {
    fun findByEmail(email: String): Mono<UserEntity>
    fun existsByEmail(email: String): Mono<Boolean>
}
