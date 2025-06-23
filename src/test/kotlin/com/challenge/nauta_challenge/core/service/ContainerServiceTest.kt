package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class ContainerServiceTest {

    private val containerRepository = mockk<ContainerRepository>()
    private val userLoggedService = mockk<UserLoggedService>()
    private val bookingRepository = mockk<BookingRepository>()
    private val containerService = ContainerService(containerRepository, userLoggedService, bookingRepository)

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

    @Test
    fun `findAllContainersForCurrentUser returns containers for user's bookings`() = runBlocking {
        // Arrange
        val userId = 100L
        val currentUser = User(id = userId, email = "user@example.com", password = "password")

        val booking1 = Booking(id = 1L, bookingNumber = "BOOK-001", userId = userId)
        val booking2 = Booking(id = 2L, bookingNumber = "BOOK-002", userId = userId)

        val container1 = Container(id = 1L, containerNumber = "CONT-001", bookingId = booking1.id!!)
        val container2 = Container(id = 2L, containerNumber = "CONT-002", bookingId = booking1.id!!)
        val container3 = Container(id = 3L, containerNumber = "CONT-003", bookingId = booking2.id!!)

        coEvery { userLoggedService.getCurrentUserId() } returns currentUser
        every { bookingRepository.findAllByUserId(userId) } returns flowOf(booking1, booking2)
        every { containerRepository.findAllByBookingId(booking1.id!!) } returns flowOf(container1, container2)
        every { containerRepository.findAllByBookingId(booking2.id!!) } returns flowOf(container3)

        // Act
        val result = containerService.findAllContainersForCurrentUser().toList()

        // Then
        assertEquals(3, result.size)
        assertEquals(container1.id, result[0].id)
        assertEquals(container1.containerNumber, result[0].containerNumber)
        assertEquals(container2.id, result[1].id)
        assertEquals(container2.containerNumber, result[1].containerNumber)
        assertEquals(container3.id, result[2].id)
        assertEquals(container3.containerNumber, result[2].containerNumber)
    }

    @Test
    fun `findAllContainersForCurrentUser returns empty flow when user has no bookings`() = runBlocking {
        // Arrange
        val userId = 100L
        val currentUser = User(id = userId, email = "user@example.com", password = "password")

        coEvery { userLoggedService.getCurrentUserId() } returns currentUser
        every { bookingRepository.findAllByUserId(userId) } returns flowOf()

        // Act
        val result = containerService.findAllContainersForCurrentUser().toList()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `findAllContainersForCurrentUser returns empty flow when bookings have no containers`() = runBlocking {
        // Arrange
        val userId = 100L
        val currentUser = User(id = userId, email = "user@example.com", password = "password")
        val booking = Booking(id = 1L, bookingNumber = "BOOK-001", userId = userId)

        coEvery { userLoggedService.getCurrentUserId() } returns currentUser
        every { bookingRepository.findAllByUserId(userId) } returns flowOf(booking)
        every { containerRepository.findAllByBookingId(booking.id!!) } returns flowOf()

        // Act
        val result = containerService.findAllContainersForCurrentUser().toList()

        // Then
        assertEquals(0, result.size)
    }
}