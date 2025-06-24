package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.model.OrderContainer
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest
class OrderContainerAssociationServiceTest {

    // Arrange
    val orderContainerRepository = mockk<OrderContainerRepository>()
    val orderContainerAssociationService = OrderContainerAssociationService(orderContainerRepository)

    @Test
    fun createAssociationsCorrectlyOneOrderToMultipleContainers() {

        val bookingNumber = "BOOK123"
        val orderId = 1L
        val containerId1 = 1L
        val containerId2 = 2L

        val orders = listOf(Order(id = orderId, purchaseNumber = "PO123", bookingId = 10, invoices = emptyList()))
        val containers = listOf(
            Container(id = containerId1, containerNumber = "CONT1", bookingId = 10),
            Container(id = containerId2, containerNumber = "CONT2", bookingId = 10)
        )

        val orderContainer1 = OrderContainer(orderId = orderId, containerId = containerId1)
        val orderContainer2 = OrderContainer(orderId = orderId, containerId = containerId2)

        every { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId1) } returns Mono.just(false)
        every { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId2) } returns Mono.just(false)
        every { orderContainerRepository.save(orderId, containerId1) } returns Mono.just(orderContainer1)
        every { orderContainerRepository.save(orderId, containerId2) } returns Mono.just(orderContainer2)

        // Act & Assert
        StepVerifier.create(orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        ))
        .verifyComplete()

        // Verify repository calls
        verify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId1) }
        verify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId2) }
        verify(exactly = 1) { orderContainerRepository.save(orderId, containerId1) }
        verify(exactly = 1) { orderContainerRepository.save(orderId, containerId2) }
    }

    @Test
    fun createAssociationsCorrectlyMultipleOrdersToOneContainer() {

        val bookingNumber = "BOOK123"
        val orderId1 = 1L
        val orderId2 = 2L
        val containerId = 1L

        val orders = listOf(
            Order(id = orderId1, purchaseNumber = "PO1", bookingId = 10, invoices = emptyList()),
            Order(id = orderId2, purchaseNumber = "PO2", bookingId = 10, invoices = emptyList())
        )
        val containers = listOf(Container(id = containerId, containerNumber = "CONT1", bookingId = 10))

        val orderContainer1 = OrderContainer(orderId = orderId1, containerId = containerId)
        val orderContainer2 = OrderContainer(orderId = orderId2, containerId = containerId)

        every { orderContainerRepository.existsByOrderIdAndContainerId(orderId1, containerId) } returns Mono.just(false)
        every { orderContainerRepository.existsByOrderIdAndContainerId(orderId2, containerId) } returns Mono.just(false)
        every { orderContainerRepository.save(orderId1, containerId) } returns Mono.just(orderContainer1)
        every { orderContainerRepository.save(orderId2, containerId) } returns Mono.just(orderContainer2)

        // Act & Assert
        StepVerifier.create(orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        ))
        .verifyComplete()

        // Verify repository calls
        verify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId1, containerId) }
        verify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId2, containerId) }
        verify(exactly = 1) { orderContainerRepository.save(orderId1, containerId) }
        verify(exactly = 1) { orderContainerRepository.save(orderId2, containerId) }
    }

    @Test
    fun doNotCreateAssociationIfAlreadyExists() {

        val bookingNumber = "BOOK123"
        val orderId = 1L
        val containerId = 1L

        val orders = listOf(Order(id = orderId, purchaseNumber = "PO123", bookingId = 10, invoices = emptyList()))
        val containers = listOf(Container(id = containerId, containerNumber = "CONT1", bookingId = 10))

        every { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId) } returns Mono.just(true)

        // Act & Assert
        StepVerifier.create(orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        ))
        .verifyComplete()

        // Verify repository calls
        verify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId) }
        verify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }

    @Test
    fun doNotCreateAssociationsWhenNoOrders() {

        val bookingNumber = "BOOK123"
        val containerId = 1L

        val orders = emptyList<Order>()
        val containers = listOf(Container(id = containerId, containerNumber = "CONT1", bookingId = 10))

        // Act & Assert
        StepVerifier.create(orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        ))
        .verifyComplete()

        // Verify repository calls
        verify(exactly = 0) { orderContainerRepository.existsByOrderIdAndContainerId(any(), any()) }
        verify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }

    @Test
    fun doNotCreateAssociationsWhenNoContainers() {

        val bookingNumber = "BOOK123"
        val orderId = 1L

        val orders = listOf(Order(id = orderId, purchaseNumber = "PO123", bookingId = 10, invoices = emptyList()))
        val containers = emptyList<Container>()

        // Act & Assert
        StepVerifier.create(orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        ))
        .verifyComplete()

        // Verify repository calls
        verify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }
}