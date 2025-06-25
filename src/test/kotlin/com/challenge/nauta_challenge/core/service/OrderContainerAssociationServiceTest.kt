package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.model.OrderContainer
import com.challenge.nauta_challenge.core.repository.OrderContainerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class OrderContainerAssociationServiceTest {

    @Test
    fun createAssociationsCorrectlyOneOrderToMultipleContainers(): Unit = runTest {
        // Arrange
        val orderContainerRepository = mockk<OrderContainerRepository>()
        val orderContainerAssociationService = OrderContainerAssociationService(orderContainerRepository)

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

        coEvery { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId1) } returns false
        coEvery { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId2) } returns false
        coEvery { orderContainerRepository.save(orderId, containerId1) } returns orderContainer1
        coEvery { orderContainerRepository.save(orderId, containerId2) } returns orderContainer2

        // Act
        orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        )

        // Assert
        coVerify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId1) }
        coVerify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId2) }
        coVerify(exactly = 1) { orderContainerRepository.save(orderId, containerId1) }
        coVerify(exactly = 1) { orderContainerRepository.save(orderId, containerId2) }
    }

    @Test
    fun createAssociationsCorrectlyMultipleOrdersToOneContainer(): Unit = runTest {
        // Arrange
        val orderContainerRepository = mockk<OrderContainerRepository>()
        val orderContainerAssociationService = OrderContainerAssociationService(orderContainerRepository)

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

        coEvery { orderContainerRepository.existsByOrderIdAndContainerId(orderId1, containerId) } returns false
        coEvery { orderContainerRepository.existsByOrderIdAndContainerId(orderId2, containerId) } returns false
        coEvery { orderContainerRepository.save(orderId1, containerId) } returns orderContainer1
        coEvery { orderContainerRepository.save(orderId2, containerId) } returns orderContainer2

        // Act
        orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        )

        // Assert
        coVerify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId1, containerId) }
        coVerify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId2, containerId) }
        coVerify(exactly = 1) { orderContainerRepository.save(orderId1, containerId) }
        coVerify(exactly = 1) { orderContainerRepository.save(orderId2, containerId) }
    }

    @Test
    fun doNotCreateAssociationIfAlreadyExists(): Unit = runTest {
        // Arrange
        val orderContainerRepository = mockk<OrderContainerRepository>()
        val orderContainerAssociationService = OrderContainerAssociationService(orderContainerRepository)

        val bookingNumber = "BOOK123"
        val orderId = 1L
        val containerId = 1L

        val orders = listOf(Order(id = orderId, purchaseNumber = "PO123", bookingId = 10, invoices = emptyList()))
        val containers = listOf(Container(id = containerId, containerNumber = "CONT1", bookingId = 10))

        coEvery { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId) } returns true

        // Act
        orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        )

        // Assert
        coVerify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId) }
        coVerify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }

    @Test
    fun doNotCreateAssociationsWhenNoOrders(): Unit = runTest {
        // Arrange
        val orderContainerRepository = mockk<OrderContainerRepository>()
        val orderContainerAssociationService = OrderContainerAssociationService(orderContainerRepository)

        val bookingNumber = "BOOK123"
        val containerId = 1L

        val orders = emptyList<Order>()
        val containers = listOf(Container(id = containerId, containerNumber = "CONT1", bookingId = 10))

        // Act
        orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        )

        // Assert
        coVerify(exactly = 0) { orderContainerRepository.existsByOrderIdAndContainerId(any(), any()) }
        coVerify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }

    @Test
    fun doNotCreateAssociationsWhenNoContainers(): Unit = runTest {
        // Arrange
        val orderContainerRepository = mockk<OrderContainerRepository>()
        val orderContainerAssociationService = OrderContainerAssociationService(orderContainerRepository)

        val bookingNumber = "BOOK123"
        val orderId = 1L

        val orders = listOf(Order(id = orderId, purchaseNumber = "PO123", bookingId = 10, invoices = emptyList()))
        val containers = emptyList<Container>()

        // Act
        orderContainerAssociationService.createAssociations(
            orders = orders,
            containers = containers,
            bookingNumber = bookingNumber
        )

        // Assert
        coVerify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }

    @Test
    fun throwExceptionWhenCheckExistsFails(): Unit = runTest {
        // Arrange
        val orderContainerRepository = mockk<OrderContainerRepository>()
        val orderContainerAssociationService = OrderContainerAssociationService(orderContainerRepository)

        val bookingNumber = "BOOK123"
        val orderId = 1L
        val containerId = 1L

        val orders = listOf(Order(id = orderId, purchaseNumber = "PO123", bookingId = 10, invoices = emptyList()))
        val containers = listOf(Container(id = containerId, containerNumber = "CONT1", bookingId = 10))

        val expectedException = RuntimeException("Error de base de datos al verificar existencia")
        coEvery { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId) } throws expectedException

        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            orderContainerAssociationService.createAssociations(
                orders = orders,
                containers = containers,
                bookingNumber = bookingNumber
            )
        }

        // Verify exception details
        assert(exception.message?.contains("Error de base de datos al verificar existencia") == true)

        // Verify repository interaction
        coVerify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId) }
        coVerify(exactly = 0) { orderContainerRepository.save(any(), any()) }
    }

    @Test
    fun throwExceptionWhenSaveFails(): Unit = runTest {
        // Arrange
        val orderContainerRepository = mockk<OrderContainerRepository>()
        val orderContainerAssociationService = OrderContainerAssociationService(orderContainerRepository)

        val bookingNumber = "BOOK123"
        val orderId = 1L
        val containerId = 1L

        val orders = listOf(Order(id = orderId, purchaseNumber = "PO123", bookingId = 10, invoices = emptyList()))
        val containers = listOf(Container(id = containerId, containerNumber = "CONT1", bookingId = 10))

        val expectedException = RuntimeException("Error de base de datos al guardar")
        coEvery { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId) } returns false
        coEvery { orderContainerRepository.save(orderId, containerId) } throws expectedException

        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            orderContainerAssociationService.createAssociations(
                orders = orders,
                containers = containers,
                bookingNumber = bookingNumber
            )
        }

        // Verify exception details
        assert(exception.message?.contains("Error de base de datos al guardar") == true)

        // Verify repository interaction
        coVerify(exactly = 1) { orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId) }
        coVerify(exactly = 1) { orderContainerRepository.save(orderId, containerId) }
    }
}