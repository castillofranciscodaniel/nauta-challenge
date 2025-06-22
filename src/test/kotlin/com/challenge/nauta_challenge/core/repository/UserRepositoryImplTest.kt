package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.UserRepositoryImpl
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.infrastructure.repository.dao.UserDao
import com.challenge.nauta_challenge.infrastructure.repository.model.UserEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import kotlin.test.*

@SpringBootTest
class UserRepositoryImplTest {

    private val userDao = mockk<UserDao>()
    private val userRepository: UserRepository = UserRepositoryImpl(userDao)

    @Test
    fun encuentraUsuarioPorEmail() = runBlocking {
        val userEntity = UserEntity(
            id = 1, email = "test@example.com", password = "password",
            createAt = LocalDateTime.now()
        )

        val user = userEntity.toModel()

        every { userDao.findByEmail("test@example.com") }.returns(Mono.just(userEntity))

        val resultado = userRepository.findByEmail("test@example.com")

        assertEquals(user, resultado)
    }

    @Test
    fun lanzaExcepcionCuandoUsuarioNoEncontrado(): Unit = runBlocking {
        every { userDao.findByEmail("test@example.com") }.returns(Mono.empty())

        val resultado = userRepository.findByEmail("test@example.com")

        assertNull(resultado)
    }

    @Test
    fun verificaExistenciaDeUsuarioPorEmail() = runBlocking {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.just(true))

        val resultado = userRepository.existsByEmail("test@example.com")

        assertTrue(resultado)
    }

    @Test
    fun devuelveFalseCuandoUsuarioNoExiste() = runBlocking {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.just(false))

        val resultado = userRepository.existsByEmail("test@example.com")

        assertFalse(resultado)
    }

    @Test
    fun devuelveFalseCuandoExistsByEmailRetornaMonoEmpty() = runBlocking {
        every { userDao.existsByEmail("test@example.com") }.returns(Mono.empty())

        val resultado = userRepository.existsByEmail("test@example.com")

        assertFalse(resultado)
    }

    @Test
    fun guardaUsuarioExitosamente() = runBlocking {
        val user = User(id = null, email = "test@example.com", password = "password")
        val userEntity = UserEntity.fromModel(user)

        every { userDao.save(userEntity) }.returns(Mono.just(userEntity.copy(id = 1)))

        val resultado = userRepository.save(user)

        assertEquals(1, resultado.id)
        assertEquals("test@example.com", resultado.email)
        assertEquals("password", resultado.password)
    }

    @Test
    fun lanzaExcepcionCuandoNoSeGuardaUsuario(): Unit = runBlocking {
        val user = User(id = null, email = "test@example.com", password = "password")
        val userEntity = UserEntity.fromModel(user)

        every { userDao.save(userEntity) }.returns(Mono.empty())

        assertFailsWith<IllegalStateException>("Failed to save user") {
            userRepository.save(user)
        }
    }
}