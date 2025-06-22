package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class ContainerServiceTest {

    private val containerRepository = mockk<ContainerRepository>()
    private val containerService = ContainerService(containerRepository)

    @Test
    fun devuelveContainerExistenteSiYaEstáAlmacenado(): Unit = runBlocking {
        // Arrange
        val bookingId = 1L
        val containerNumber = "CONT12345"
        val existingContainer = Container(
            id = 1L,
            containerNumber = containerNumber,
            bookingId = bookingId
        )
        val containers = listOf(
            Container(
                id = null,
                containerNumber = containerNumber,
                bookingId = null
            )
        )

        coEvery { containerRepository.findByContainerNumberAndBookingId(containerNumber, bookingId) } returns existingContainer

        // Act
        val result = containerService.saveContainersForBooking(containers, bookingId)

        // Assert
        assertEquals(1, result.size)
        assertEquals(existingContainer.id, result[0].id)
        assertEquals(containerNumber, result[0].containerNumber)
        assertEquals(bookingId, result[0].bookingId)
    }

    @Test
    fun guardaContainerCuandoNoExiste(): Unit = runBlocking {
        // Arrange
        val bookingId = 1L
        val containerNumber = "CONT12345"
        val container = Container(
            id = null,
            containerNumber = containerNumber,
            bookingId = null
        )
        val savedContainer = Container(
            id = 1L,
            containerNumber = containerNumber,
            bookingId = bookingId
        )
        val containers = listOf(container)

        coEvery { containerRepository.findByContainerNumberAndBookingId(containerNumber, bookingId) } returns null
        coEvery { containerRepository.save(container.copy(bookingId = bookingId)) } returns savedContainer

        // Act
        val result = containerService.saveContainersForBooking(containers, bookingId)

        // Assert
        assertEquals(1, result.size)
        assertEquals(savedContainer.id, result[0].id)
        assertEquals(containerNumber, result[0].containerNumber)
        assertEquals(bookingId, result[0].bookingId)
    }

    @Test
    fun procesaMultiplesContainersCorrectamente(): Unit = runBlocking {
        // Arrange
        val bookingId = 1L
        val containerNumber1 = "CONT12345"
        val containerNumber2 = "CONT67890"
        
        val container1 = Container(id = null, containerNumber = containerNumber1, bookingId = null)
        val container2 = Container(id = null, containerNumber = containerNumber2, bookingId = null)
        
        val existingContainer = Container(id = 1L, containerNumber = containerNumber1, bookingId = bookingId)
        val savedContainer = Container(id = 2L, containerNumber = containerNumber2, bookingId = bookingId)
        
        val containers = listOf(container1, container2)

        coEvery { containerRepository.findByContainerNumberAndBookingId(containerNumber1, bookingId) } returns existingContainer
        coEvery { containerRepository.findByContainerNumberAndBookingId(containerNumber2, bookingId) } returns null
        coEvery { containerRepository.save(container2.copy(bookingId = bookingId)) } returns savedContainer

        // Act
        val result = containerService.saveContainersForBooking(containers, bookingId)

        // Assert
        assertEquals(2, result.size)
        assertEquals(existingContainer.id, result[0].id)
        assertEquals(containerNumber1, result[0].containerNumber)
        assertEquals(savedContainer.id, result[1].id)
        assertEquals(containerNumber2, result[1].containerNumber)
    }
}