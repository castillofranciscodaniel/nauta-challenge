package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.UserRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.infrastructure.repository.dao.UserDao
import com.challenge.nauta_challenge.infrastructure.repository.model.UserEntity
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import kotlin.test.*

@SpringBootTest
class UserRepositoryImplTest {

    private val userDao = mockk<UserDao>()
    private val userRepository: UserRepository = UserRepositoryImpl(userDao)

    @Test
    fun findsUserByEmail() {
        val userEntity = UserEntity(
            id = 1,
            email = "test@example.com",
            password = "password",
            createAt = LocalDateTime.now()
        )

        every { userDao.findByEmail("test@example.com") }.returns(Mono.just(userEntity))

        StepVerifier.create(userRepository.findByEmail("test@example.com"))
            .assertNext { result ->
                assertEquals(1, result.id)
                assertEquals("test@example.com", result.email)
                assertEquals("password", result.password)
            }
            .verifyComplete()
    }

    @Test
    fun returnsEmptyMonoWhenUserNotFound() {
        every { userDao.findByEmail("test@example.com") }.returns(Mono.empty())

        StepVerifier.create(userRepository.findByEmail("test@example.com"))
            .verifyComplete()
    }

    @Test
    fun checksUserExistenceByEmail() {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.just(true))

        StepVerifier.create(userRepository.existsByEmail("test@example.com"))
            .assertNext { result ->
                assertTrue(result)
            }
            .verifyComplete()
    }

    @Test
    fun returnsFalseWhenUserDoesNotExist() {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.just(false))

        StepVerifier.create(userRepository.existsByEmail("test@example.com"))
            .assertNext { result ->
                assertFalse(result)
            }
            .verifyComplete()
    }

    @Test
    fun returnsFalseWhenExistsByEmailReturnsMonoEmpty() {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.empty())

        StepVerifier.create(userRepository.existsByEmail("test@example.com"))
            .assertNext { result ->
                assertFalse(result)
            }
            .verifyComplete()
    }

    @Test
    fun savesUserSuccessfully() {
        val user = User(
            id = null,
            email = "test@example.com",
            password = "password"
        )
        val userEntity = UserEntity.fromModel(user)
        val savedUserEntity = userEntity.copy(id = 1)

        every { userDao.save(userEntity) }.returns(Mono.just(savedUserEntity))

        StepVerifier.create(userRepository.save(user))
            .assertNext { result ->
                assertEquals(1, result.id)
                assertEquals("test@example.com", result.email)
                assertEquals("password", result.password)
            }
            .verifyComplete()
    }

    @Test
    fun throwsExceptionWhenUserNotSaved() {
        val user = User(
            id = null,
            email = "test@example.com",
            password = "password"
        )
        val userEntity = UserEntity.fromModel(user)

        every { userDao.save(userEntity) }.returns(Mono.empty())

        StepVerifier.create(userRepository.save(user))
            .expectError(ModelNotSavedException::class.java)
            .verify()
    }

    @Test
    fun throwsRepositoryExceptionWhenErrorDuringExistsByEmail() {
        // Arrange
        val email = "test@example.com"

        // Simular una excepción durante la llamada al DAO
        every { userDao.existsByEmail(email) }.returns(Mono.error(RuntimeException("Database error during check")))

        // Act & Assert
        StepVerifier.create(userRepository.existsByEmail(email))
            .expectError(RepositoryException::class.java)
            .verify()
    }
}