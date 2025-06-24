package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.UserRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.UserDao
import com.challenge.nauta_challenge.infrastructure.repository.model.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    private val logger = LoggerFactory.getLogger(UserRepositoryImpl::class.java)

    override fun findByEmail(email: String): Mono<User> {
        logger.debug("[findByEmail] Looking for user with email=$email")

        return userDao.findByEmail(email)
            .map { userEntity -> userEntity.toModel() }
            .doOnSuccess { user ->
                if (user != null) {
                    logger.debug("[findByEmail] Found user: id=${user.id}, email=$email")
                } else {
                    logger.debug("[findByEmail] User not found with email=$email")
                }
            }
            .onErrorMap { e ->
                logger.warn("[findByEmail] Error while looking for user with email=$email", e)
                RepositoryException("Error finding user by email", e)
            }
    }

    override fun existsByEmail(email: String): Mono<Boolean> {
        logger.debug("[existsByEmail] Checking if user exists with email=$email")

        return userDao.existsByEmail(email)
            .defaultIfEmpty(false)
            .doOnSuccess { exists -> logger.debug("[existsByEmail] User exists=$exists with email=$email") }
            .onErrorMap { e ->
                logger.warn("[existsByEmail] Error checking if user exists with email=$email", e)
                RepositoryException("Error checking if user exists", e)
            }
    }

    override fun save(user: User): Mono<User> {
        logger.info("[save] Attempting to save user: email=${user.email}")

        return userDao.save(UserEntity.fromModel(user))
            .map { it.toModel() }
            .doOnSuccess { logger.info("[save] Successfully saved user: id=${it.id}, email=${it.email}") }
            .switchIfEmpty(Mono.error(ModelNotSavedException("Failed to save user")))
            .onErrorMap { e ->
                logger.error("[save] Error while saving user: email=${user.email}", e)
                ModelNotSavedException("Failed to save user: ${e.message}")
            }
    }
}
