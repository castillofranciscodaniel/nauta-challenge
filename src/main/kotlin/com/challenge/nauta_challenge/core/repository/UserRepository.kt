package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.core.model.User
import org.springframework.stereotype.Repository

@Repository
interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun existsByEmail(email: String): Boolean
    suspend fun save(user: User): User
}
