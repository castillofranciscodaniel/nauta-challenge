package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.model.OrderContainer
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class OrderContainerAssociationServiceTest {

    private val orderContainerRepository = mockk<OrderContainerRepository>()
    private val orderContainerAssociationService = OrderContainerAssociationService(orderContainerRepository)

    @Test
    fun creaAsociacionesCorrectamenteUnaOrdenAMultiplesContenedores(): Unit = runBlocking {
        // Arrange
        val order = Order(id = 1L, purchaseNumber = "PO-123", bookingId = 10L, invoices = emptyList())
        val container1 = Container(id = 101L, containerNumber = "CONT-001", bookingId = 10L)
        val container2 = Container(id = 102L, containerNumber = "CONT-002", bookingId = 10L)
        val containers = listOf(container1, container2)
        val bookingNumber = "BOOK-123"

        val orderContainer_1 = OrderContainer(
            orderId = 1L,
            containerId = 101L
        )
        val orderContainer_2 = OrderContainer(
            orderId = 1L,
            containerId = 102L
        )
        
        coEvery { 
            orderContainerRepository.existsByOrderIdAndContainerId(1L, 101L) 
        } returns false
        
        coEvery { 
            orderContainerRepository.existsByOrderIdAndContainerId(1L, 102L) 
        } returns false
        
        coEvery { 
            orderContainerRepository.save(1L, 101L) 
        } returns orderContainer_1
        
        coEvery { 
            orderContainerRepository.save(1L, 102L) 
        } returns orderContainer_2

        // Act
        orderContainerAssociationService.createAssociations(listOf(order), containers, bookingNumber)

        // Assert
        coVerify(exactly = 1) { orderContainerRepository.save(1L, 101L) }
        coVerify(exactly = 1) { orderContainerRepository.save(1L, 102L) }
    }

    @Test
    fun creaAsociacionesCorrectamenteMultiplesOrdenesAUnContenedor(): Unit = runBlocking {
        // Arrange
        val order1 = Order(id = 1L, purchaseNumber = "PO-123", bookingId = 10L, invoices = emptyList())
        val order2 = Order(id = 2L, purchaseNumber = "PO-456", bookingId = 10L, invoices = emptyList())
        val orders = listOf(order1, order2)
        val container = Container(id = 101L, containerNumber = "CONT-001", bookingId = 10L)
        val bookingNumber = "BOOK-123"

        val orderContainer_1 = OrderContainer(
            orderId = 1L,
            containerId = 101L
        )

        val orderContainer_2 = OrderContainer(
            orderId = 2L,
            containerId = 101L
        )
        
        coEvery { 
            orderContainerRepository.existsByOrderIdAndContainerId(1L, 101L) 
        } returns false
        
        coEvery { 
            orderContainerRepository.existsByOrderIdAndContainerId(2L, 101L) 
        } returns false
        
        coEvery { 
            orderContainerRepository.save(1L, 101L) 
        } returns orderContainer_1
        
        coEvery { 
            orderContainerRepository.save(2L, 101L) 
        } returns orderContainer_2

        // Act
        orderContainerAssociationService.createAssociations(orders, listOf(container), bookingNumber)

        // Assert
        coVerify(exactly = 1) { orderContainerRepository.save(1L, 101L) }
        coVerify(exactly = 1) { orderContainerRepository.save(2L, 101L) }
    }

    @Test
    fun noCrearAsociacionSiYaExiste(): Unit = runBlocking {
        // Arrange
        val order = Order(id = 1L, purchaseNumber = "PO-123", bookingId = 10L, invoices = emptyList())
        val container = Container(id = 101L, containerNumber = "CONT-001", bookingId = 10L)
        val bookingNumber = "BOOK-123"
        
        coEvery { 
            orderContainerRepository.existsByOrderIdAndContainerId(1L, 101L) 
        } returns true

        // Act
        orderContainerAssociationService.createAssociations(listOf(order), listOf(container), bookingNumber)

        // Assert
        coVerify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }

    @Test
    fun noCrearAsociacionesCuandoHayRelacionAmbigua(): Unit = runBlocking {
        // Arrange
        val order1 = Order(id = 1L, purchaseNumber = "PO-123", bookingId = 10L, invoices = emptyList())
        val order2 = Order(id = 2L, purchaseNumber = "PO-456", bookingId = 10L, invoices = emptyList())
        val container1 = Container(id = 101L, containerNumber = "CONT-001", bookingId = 10L)
        val container2 = Container(id = 102L, containerNumber = "CONT-002", bookingId = 10L)
        val bookingNumber = "BOOK-123"

        // Act
        orderContainerAssociationService.createAssociations(
            listOf(order1, order2), 
            listOf(container1, container2), 
            bookingNumber
        )

        // Assert
        coVerify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }

    @Test
    fun noCrearAsociacionesCuandoNoHayOrdenes(): Unit = runBlocking {
        // Arrange
        val container = Container(id = 101L, containerNumber = "CONT-001", bookingId = 10L)
        val bookingNumber = "BOOK-123"

        // Act
        orderContainerAssociationService.createAssociations(
            emptyList(), 
            listOf(container), 
            bookingNumber
        )

        // Assert
        coVerify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }

    @Test
    fun noCrearAsociacionesCuandoNoHayContenedores(): Unit = runBlocking {
        // Arrange
        val order = Order(id = 1L, purchaseNumber = "PO-123", bookingId = 10L, invoices = emptyList())
        val bookingNumber = "BOOK-123"

        // Act
        orderContainerAssociationService.createAssociations(
            listOf(order), 
            emptyList(), 
            bookingNumber
        )

        // Assert
        coVerify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }
}