package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.ContainerRepositoryImpl
import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.infrastructure.repository.dao.ContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.ContainerEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@SpringBootTest
class ContainerRepositoryImplTest {

    private val containerDao = mockk<ContainerDao>()
    private val containerRepository: ContainerRepository = ContainerRepositoryImpl(containerDao)

    @Test
    fun guardaContenedorExitosamente() = runBlocking {
        val container = Container(id = null, containerNumber = "CONT123", bookingId = 1)
        val containerEntity = ContainerEntity(id = null, containerNumber = "CONT123", bookingId = 1)

        every { containerDao.save(containerEntity) }.returns(Mono.just(containerEntity.copy(id = 1)))

        val resultado = containerRepository.save(container)

        assertEquals(1, resultado.id)
        assertEquals("CONT123", resultado.containerNumber)
        assertEquals(1, resultado.bookingId)
    }

    @Test
    fun lanzaExcepcionCuandoNoSeGuardaContenedor(): Unit = runBlocking {
        val container = Container(id = null, containerNumber = "CONT123", bookingId = 1)
        val containerEntity = ContainerEntity(id = null, containerNumber = "CONT123", bookingId = 1)

        every { containerDao.save(containerEntity) }.returns(Mono.empty())

        assertFailsWith<Exception>("Container not saved") {
            containerRepository.save(container)
        }
    }

    @Test
    fun encuentraContenedorPorNumeroYBookingId() = runBlocking {
        val containerEntity = ContainerEntity(id = 1, containerNumber = "CONT123", bookingId = 1)

        every { containerDao.findByContainerNumberAndBookingId("CONT123", 1) }
            .returns(Mono.just(containerEntity))

        val resultado = containerRepository.findByContainerNumberAndBookingId("CONT123", 1)

        assertEquals(1, resultado?.id)
        assertEquals("CONT123", resultado?.containerNumber)
        assertEquals(1, resultado?.bookingId)
    }

    @Test
    fun devuelveNullCuandoNoEncuentraContenedor() = runBlocking {
        every { containerDao.findByContainerNumberAndBookingId("CONT123", 1) }.returns(Mono.empty())

        val resultado = containerRepository.findByContainerNumberAndBookingId("CONT123", 1)

        assertNull(resultado)
    }
}