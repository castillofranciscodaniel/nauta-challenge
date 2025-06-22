package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.UserRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.UserDao
import com.challenge.nauta_challenge.infrastructure.repository.model.UserEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun findByEmail(email: String): User? =
        userDao.findByEmail(email)
            .map { userEntity -> userEntity.toModel() }
            .awaitSingleOrNull()


    override suspend fun existsByEmail(email: String): Boolean =
        userDao.existsByEmail(email)
            .awaitSingleOrNull() ?: false

    override suspend fun save(user: User): User {
        val userEntity = userDao.save(UserEntity.fromModel(user))
            .awaitSingleOrNull() ?: throw IllegalStateException("Failed to save user")

        return userEntity.toModel()
    }

}
