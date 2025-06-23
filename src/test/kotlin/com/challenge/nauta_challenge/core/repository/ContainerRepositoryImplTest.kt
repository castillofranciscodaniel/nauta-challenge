package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.ContainerRepositoryImpl
import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.infrastructure.repository.dao.ContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.ContainerEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
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
    fun guardaContenedorExitosamente() = runTest {
        val container = Container(id = null, containerNumber = "CONT123", bookingId = 1)
        val containerEntity = ContainerEntity(id = null, containerNumber = "CONT123", bookingId = 1)

        every { containerDao.save(containerEntity) }.returns(Mono.just(containerEntity.copy(id = 1)))

        val resultado = containerRepository.save(container)

        assertEquals(1, resultado.id)
        assertEquals("CONT123", resultado.containerNumber)
        assertEquals(1, resultado.bookingId)
    }

    @Test
    fun lanzaExcepcionCuandoNoSeGuardaContenedor(): Unit = runTest {
        val container = Container(id = null, containerNumber = "CONT123", bookingId = 1)
        val containerEntity = ContainerEntity(id = null, containerNumber = "CONT123", bookingId = 1)

        every { containerDao.save(containerEntity) }.returns(Mono.empty())

        assertFailsWith<Exception>("Container not saved") {
            containerRepository.save(container)
        }
    }

    @Test
    fun encuentraContenedorPorNumeroYBookingId() = runTest {
        val containerEntity = ContainerEntity(id = 1, containerNumber = "CONT123", bookingId = 1)

        every { containerDao.findByContainerNumberAndBookingId("CONT123", 1) }
            .returns(Mono.just(containerEntity))

        val resultado = containerRepository.findByContainerNumberAndBookingId("CONT123", 1)

        assertEquals(1, resultado?.id)
        assertEquals("CONT123", resultado?.containerNumber)
        assertEquals(1, resultado?.bookingId)
    }

    @Test
    fun devuelveNullCuandoNoEncuentraContenedor() = runTest {
        every { containerDao.findByContainerNumberAndBookingId("CONT123", 1) }.returns(Mono.empty())

        val resultado = containerRepository.findByContainerNumberAndBookingId("CONT123", 1)

        assertNull(resultado)
    }

    @Test
    fun `findAllByBookingIds returns flow of containers from bookings`() = runTest {
        // Arrange
        val bookingIds = listOf(1L, 2L)
        val containerEntity1 = ContainerEntity(id = 1, containerNumber = "CONT-001", bookingId = 1)
        val containerEntity2 = ContainerEntity(id = 2, containerNumber = "CONT-002", bookingId = 1)
        val containerEntity3 = ContainerEntity(id = 3, containerNumber = "CONT-003", bookingId = 2)

        every { containerDao.findAllByBookingIdIn(bookingIds) } returns
            reactor.core.publisher.Flux.just(containerEntity1, containerEntity2, containerEntity3)

        // Act
        val result = containerRepository.findAllByBookingIds(bookingIds).toList()

        // Assert
        assertEquals(3, result.size)
        assertEquals(1, result[0].id)
        assertEquals("CONT-001", result[0].containerNumber)
        assertEquals(1, result[0].bookingId)
        assertEquals(2, result[1].id)
        assertEquals("CONT-002", result[1].containerNumber)
        assertEquals(1, result[1].bookingId)
        assertEquals(3, result[2].id)
        assertEquals("CONT-003", result[2].containerNumber)
        assertEquals(2, result[2].bookingId)
    }

    @Test
    fun `findAllByBookingIds returns empty flow when no containers found`() = runTest {
        // Arrange
        val bookingIds = listOf(1L, 2L)

        every { containerDao.findAllByBookingIdIn(bookingIds) } returns
            reactor.core.publisher.Flux.empty()

        // Act
        val result = containerRepository.findAllByBookingIds(bookingIds).toList()

        // Assert
        assertEquals(0, result.size)
    }

    @Test
    fun `findContainersByPurchaseNumberAndUserId returns containers for specific purchase and user`() = runTest {
        // Arrange
        val purchaseNumber = "PO-123"
        val userId = 10L
        val containerEntity1 = ContainerEntity(id = 1, containerNumber = "CONT-001", bookingId = 5L)
        val containerEntity2 = ContainerEntity(id = 2, containerNumber = "CONT-002", bookingId = 5L)

        every { containerDao.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) } returns
            Flux.just(containerEntity1, containerEntity2)

        // Act
        val result = containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId).toList()

        // Assert
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("CONT-001", result[0].containerNumber)
        assertEquals(5L, result[0].bookingId)
        assertEquals(2L, result[1].id)
        assertEquals("CONT-002", result[1].containerNumber)
        assertEquals(5L, result[1].bookingId)
    }

    @Test
    fun `findContainersByPurchaseNumberAndUserId returns empty flow when no containers found`() = runTest {
        // Arrange
        val purchaseNumber = "PO-123"
        val userId = 10L

        every { containerDao.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) } returns Flux.empty()

        // Act
        val result = containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId).toList()

        // Assert
        assertEquals(0, result.size)
    }
}