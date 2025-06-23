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
    fun encuentraUsuarioPorEmail() = runTest {
        val userEntity = UserEntity(
            id = 1,
            email = "test@example.com",
            password = "password",
            createAt = LocalDateTime.now()
        )

        every { userDao.findByEmail("test@example.com") }.returns(Mono.just(userEntity))

        val resultado = userRepository.findByEmail("test@example.com")

        assertEquals(1, resultado?.id)
        assertEquals("test@example.com", resultado?.email)
        assertEquals("password", resultado?.password)
    }

    @Test
    fun lanzaExcepcionCuandoUsuarioNoEncontrado(): Unit = runTest {
        every { userDao.findByEmail("test@example.com") }.returns(Mono.empty())

        val resultado = userRepository.findByEmail("test@example.com")

        assertNull(resultado)
    }

    @Test
    fun verificaExistenciaDeUsuarioPorEmail() = runTest {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.just(true))

        val resultado = userRepository.existsByEmail("test@example.com")

        assertTrue(resultado)
    }

    @Test
    fun devuelveFalseCuandoUsuarioNoExiste() = runTest {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.just(false))

        val resultado = userRepository.existsByEmail("test@example.com")

        assertFalse(resultado)
    }

    @Test
    fun devuelveFalseCuandoExistsByEmailRetornaMonoEmpty() = runTest {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.empty())

        val resultado = userRepository.existsByEmail("test@example.com")

        assertFalse(resultado)
    }

    @Test
    fun guardaUsuarioExitosamente() = runTest {
        val user = User(
            id = null,
            email = "test@example.com",
            password = "password"
        )
        val userEntity = UserEntity.fromModel(user)
        val savedUserEntity = userEntity.copy(id = 1)

        every { userDao.save(userEntity) }.returns(Mono.just(savedUserEntity))

        val resultado = userRepository.save(user)

        assertEquals(1, resultado.id)
        assertEquals("test@example.com", resultado.email)
        assertEquals("password", resultado.password)
    }

    @Test
    fun lanzaExcepcionCuandoNoSeGuardaUsuario(): Unit = runTest {
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