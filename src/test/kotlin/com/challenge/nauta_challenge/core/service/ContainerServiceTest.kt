package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
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
    private val orderRepository = mockk<OrderRepository>()
    private val invoiceService = mockk<InvoiceService>()
    private val containerService = ContainerService(
        containerRepository,
        userLoggedService,
        orderRepository,
        invoiceService
    )

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

        coEvery {
            containerRepository.findByContainerNumberAndBookingId(
                containerNumber,
                bookingId
            )
        } returns existingContainer

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

        coEvery {
            containerRepository.findByContainerNumberAndBookingId(
                containerNumber1,
                bookingId
            )
        } returns existingContainer
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

        val container1 = Container(id = 1L, containerNumber = "CONT-001", bookingId = 1L)
        val container2 = Container(id = 2L, containerNumber = "CONT-002", bookingId = 1L)
        val container3 = Container(id = 3L, containerNumber = "CONT-003", bookingId = 2L)

        coEvery { userLoggedService.getCurrentUserId() } returns currentUser
        every { containerRepository.findAllByUserId(userId) } returns flowOf(container1, container2, container3)

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
    fun `findAllContainersForCurrentUser returns empty flow when user has no containers`() = runBlocking {
        // Arrange
        val userId = 100L
        val currentUser = User(id = userId, email = "user@example.com", password = "password")

        coEvery { userLoggedService.getCurrentUserId() } returns currentUser
        every { containerRepository.findAllByUserId(userId) } returns flowOf()

        // Act
        val result = containerService.findAllContainersForCurrentUser().toList()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `findOrdersByContainerId returns orders for a specific container`() = runBlocking {
        // Arrange
        val userId = 100L
        val containerId = "CONT-001"
        val currentUser = User(id = userId, email = "user@example.com", password = "password")

        val order1 = Order(id = 1L, purchaseNumber = "PO-001", bookingId = 1L)
        val order2 = Order(id = 2L, purchaseNumber = "PO-002", bookingId = 1L)

        val invoice1 = Invoice(id = 1L, invoiceNumber = "INV-001", orderId = order1.id!!)
        val invoice2 = Invoice(id = 2L, invoiceNumber = "INV-002", orderId = order2.id!!)

        coEvery { userLoggedService.getCurrentUserId() } returns currentUser
        every { orderRepository.findOrdersByContainerIdAndUserId(containerId, userId) } returns flowOf(order1, order2)
        every { invoiceService.findAllByOrderId(order1.id!!) } returns flowOf(invoice1)
        every { invoiceService.findAllByOrderId(order2.id!!) } returns flowOf(invoice2)

        // Act
        val result = containerService.findOrdersByContainerId(containerId).toList()

        // Then
        assertEquals(2, result.size)
        assertEquals(order1.id, result[0].id)
        assertEquals(order1.purchaseNumber, result[0].purchaseNumber)
        assertEquals(1, result[0].invoices.size)
        assertEquals(invoice1.id, result[0].invoices[0].id)
        assertEquals(order2.id, result[1].id)
        assertEquals(order2.purchaseNumber, result[1].purchaseNumber)
        assertEquals(1, result[1].invoices.size)
        assertEquals(invoice2.id, result[1].invoices[0].id)
    }

    @Test
    fun `findOrdersByContainerId returns empty flow when no orders found`() = runBlocking {
        // Arrange
        val userId = 100L
        val containerId = "CONT-001"
        val currentUser = User(id = userId, email = "user@example.com", password = "password")

        coEvery { userLoggedService.getCurrentUserId() } returns currentUser
        every { orderRepository.findOrdersByContainerIdAndUserId(containerId, userId) } returns flowOf()

        // Act
        val result = containerService.findOrdersByContainerId(containerId).toList()

        // Then
        assertEquals(0, result.size)
    }
}