package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.ContainerRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.infrastructure.repository.dao.ContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.ContainerEntity
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.*

@SpringBootTest
class ContainerRepositoryImplTest {

    private val containerDao = mockk<ContainerDao>()
    private val containerRepository: ContainerRepository = ContainerRepositoryImpl(containerDao)

    @Test
    fun savesContainerSuccessfully() {
        val container = Container(id = null, containerNumber = "CONT123", bookingId = 1)
        val containerEntity = ContainerEntity(id = null, containerNumber = "CONT123", bookingId = 1)

        every { containerDao.save(containerEntity) }.returns(Mono.just(containerEntity.copy(id = 1)))

        StepVerifier.create(containerRepository.save(container))
            .assertNext { result ->
                assertEquals(1, result.id)
                assertEquals("CONT123", result.containerNumber)
                assertEquals(1, result.bookingId)
            }
            .verifyComplete()
    }

    @Test
    fun throwsExceptionWhenContainerNotSaved() {
        val container = Container(id = null, containerNumber = "CONT123", bookingId = 1)
        val containerEntity = ContainerEntity(id = null, containerNumber = "CONT123", bookingId = 1)

        every { containerDao.save(containerEntity) }.returns(Mono.empty())

        StepVerifier.create(containerRepository.save(container))
            .expectError(ModelNotSavedException::class.java)
            .verify()
    }

    @Test
    fun findsContainerByNumberAndBookingId() {
        val containerEntity = ContainerEntity(id = 1, containerNumber = "CONT123", bookingId = 1)

        every { containerDao.findByContainerNumberAndBookingId("CONT123", 1) }
            .returns(Mono.just(containerEntity))

        StepVerifier.create(containerRepository.findByContainerNumberAndBookingId("CONT123", 1))
            .assertNext { result ->
                assertEquals(1, result.id)
                assertEquals("CONT123", result.containerNumber)
                assertEquals(1, result.bookingId)
            }
            .verifyComplete()
    }

    @Test
    fun returnsEmptyMonoWhenContainerNotFound() {
        every { containerDao.findByContainerNumberAndBookingId("CONT123", 1) }.returns(Mono.empty())

        StepVerifier.create(containerRepository.findByContainerNumberAndBookingId("CONT123", 1))
            .verifyComplete()
    }

    @Test
    fun `findContainersByPurchaseNumberAndUserId returns containers for specific purchase and user`() {
        // Arrange
        val purchaseNumber = "PO-123"
        val userId = 10L
        val containerEntity1 = ContainerEntity(id = 1, containerNumber = "CONT-001", bookingId = 5L)
        val containerEntity2 = ContainerEntity(id = 2, containerNumber = "CONT-002", bookingId = 5L)

        every { containerDao.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) } returns
            Flux.just(containerEntity1, containerEntity2)

        // Act & Assert
        StepVerifier.create(containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId))
            .assertNext { container ->
                assertEquals(1L, container.id)
                assertEquals("CONT-001", container.containerNumber)
                assertEquals(5L, container.bookingId)
            }
            .assertNext { container ->
                assertEquals(2L, container.id)
                assertEquals("CONT-002", container.containerNumber)
                assertEquals(5L, container.bookingId)
            }
            .verifyComplete()
    }

    @Test
    fun `findContainersByPurchaseNumberAndUserId returns empty flow when no containers found`() {
        // Arrange
        val purchaseNumber = "PO-123"
        val userId = 10L

        every { containerDao.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) } returns Flux.empty()

        // Act & Assert
        StepVerifier.create(containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId))
            .verifyComplete()
    }

    @Test
    fun `throws RepositoryException when error during findAllByUserId`() {
        // Arrange
        val userId = 1L

        // Simular una excepción durante la llamada al DAO
        every { containerDao.findAllByUserId(userId) }.returns(Flux.error(RuntimeException("Database error during flow operation")))

        // Act & Assert
        StepVerifier.create(containerRepository.findAllByUserId(userId))
            .expectError(RepositoryException::class.java)
            .verify()
    }
}