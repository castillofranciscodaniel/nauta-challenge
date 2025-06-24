package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.UserRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.UserDao
import com.challenge.nauta_challenge.infrastructure.repository.model.UserEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    private val logger = LoggerFactory.getLogger(UserRepositoryImpl::class.java)

    override suspend fun findByEmail(email: String): User? {
        logger.debug("[findByEmail] Looking for user with email=$email")

        return runCatching {
            userDao.findByEmail(email)
                .map { userEntity -> userEntity.toModel() }
                .awaitSingleOrNull()
                ?.also { logger.debug("[findByEmail] Found user: id=${it.id}, email=$email") }
                ?: run {
                    logger.debug("[findByEmail] User not found with email=$email")
                    null
                }
        }.getOrElse { e ->
            logger.warn("[findByEmail] Error while looking for user with email=$email", e)
            throw RepositoryException("Error finding user by email", e)
        }
    }

    override suspend fun existsByEmail(email: String): Boolean {
        logger.debug("[existsByEmail] Checking if user exists with email=$email")

        return runCatching {
            userDao.existsByEmail(email)
                .awaitSingleOrNull()
                ?.also { exists -> logger.debug("[existsByEmail] User exists=$exists with email=$email") }
                ?: run {
                    logger.debug("[existsByEmail] Received empty response when checking email=$email, returning false")
                    false
                }
        }.getOrElse { e ->
            logger.warn("[existsByEmail] Error checking if user exists with email=$email", e)
            throw RepositoryException("Error checking if user exists", e)
        }
    }

    override suspend fun save(user: User): User {
        logger.info("[save] Attempting to save user: email=${user.email}")

        return runCatching {
            userDao.save(UserEntity.fromModel(user))
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.info("[save] Successfully saved user: id=${it.id}, email=${it.email}") }
                ?: throw ModelNotSavedException("Failed to save user")
        }.getOrElse { e ->
            logger.error("[save] Error while saving user: email=${user.email}", e)
            throw ModelNotSavedException("Failed to save user: ${e.message}")
        }
    }
}
