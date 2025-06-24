package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.UserRepositoryImpl
import com.challenge.nauta_challenge.core.exception.NotFoundException
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.infrastructure.repository.dao.UserDao
import com.challenge.nauta_challenge.infrastructure.repository.model.UserEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import kotlin.test.*

@SpringBootTest
class UserRepositoryImplTest {

    private val userDao = mockk<UserDao>()
    private val userRepository: UserRepository = UserRepositoryImpl(userDao)

    @Test
    fun findsUserByEmail() = runTest {
        val userEntity = UserEntity(
            id = 1,
            email = "test@example.com",
            password = "password",
            createAt = LocalDateTime.now()
        )

        every { userDao.findByEmail("test@example.com") }.returns(Mono.just(userEntity))

        val result = userRepository.findByEmail("test@example.com")

        assertEquals(1, result?.id)
        assertEquals("test@example.com", result?.email)
        assertEquals("password", result?.password)
    }

    @Test
    fun returnsNullWhenUserNotFound(): Unit = runTest {
        every { userDao.findByEmail("test@example.com") }.returns(Mono.empty())

        val result = userRepository.findByEmail("test@example.com")

        assertNull(result)
    }

    @Test
    fun checksUserExistenceByEmail() = runTest {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.just(true))

        val result = userRepository.existsByEmail("test@example.com")

        assertTrue(result)
    }

    @Test
    fun returnsFalseWhenUserDoesNotExist() = runTest {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.just(false))

        val result = userRepository.existsByEmail("test@example.com")

        assertFalse(result)
    }

    @Test
    fun returnsFalseWhenExistsByEmailReturnsMonoEmpty() = runTest {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.empty())

        val result = userRepository.existsByEmail("test@example.com")

        assertFalse(result)
    }

    @Test
    fun savesUserSuccessfully() = runTest {
        val user = User(
            id = null,
            email = "test@example.com",
            password = "password"
        )
        val userEntity = UserEntity.fromModel(user)
        val savedUserEntity = userEntity.copy(id = 1)

        every { userDao.save(userEntity) }.returns(Mono.just(savedUserEntity))

        val result = userRepository.save(user)

        assertEquals(1, result.id)
        assertEquals("test@example.com", result.email)
        assertEquals("password", result.password)
    }

    @Test
    fun throwsExceptionWhenUserNotSaved(): Unit = runTest {
        val user = User(
            id = null,
            email = "test@example.com",
            password = "password"
        )
        val userEntity = UserEntity.fromModel(user)

        every { userDao.save(userEntity) }.returns(Mono.empty())

        assertFailsWith<IllegalStateException>("User not saved") {
            userRepository.save(user)
        }
    }
}