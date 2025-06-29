package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.ContainerService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class ContainerControllerTest {

    private val containerService = mockk<ContainerService>()
    private val containerController = ContainerController(containerService)

    @Test
    fun `getAllContainers should return flow of containers from service`() = runTest {
        // Given
        val container1 = Container(id = 1L, containerNumber = "CONT-001", bookingId = 100L)
        val container2 = Container(id = 2L, containerNumber = "CONT-002", bookingId = 100L)
        val containersFlow: Flow<Container> = flowOf(container1, container2)

        coEvery { containerService.findAllContainersForCurrentUser() }.returns(containersFlow)

        // When
        val result = containerController.getAllContainers()

        // Then
        val resultList = result.toList()
        assertEquals(2, resultList.size)
        assertEquals(container1.id, resultList[0].id)
        assertEquals(container1.containerNumber, resultList[0].containerNumber)
        assertEquals(container2.id, resultList[1].id)
        assertEquals(container2.containerNumber, resultList[1].containerNumber)
    }

    @Test
    fun `getAllContainers should return empty flow when no containers exist`() = runTest {
        // Given
        val emptyContainersFlow: Flow<Container> = flowOf()

        coEvery { containerService.findAllContainersForCurrentUser() }.returns(emptyContainersFlow)

        // When
        val result = containerController.getAllContainers()

        // Then
        val resultList = result.toList()
        assertEquals(0, resultList.size)
    }

    @Test
    fun `getOrdersByContainerId should return flow of orders for specific container`() = runTest {
        // Given
        val containerId = "CONT-001"
        val order1 = Order(id = 1L, purchaseNumber = "PO-001", bookingId = 100L)
        val order2 = Order(id = 2L, purchaseNumber = "PO-002", bookingId = 100L)
        val ordersFlow: Flow<Order> = flowOf(order1, order2)

        coEvery { containerService.findOrdersByContainerId(containerId) }.returns(ordersFlow)

        // When
        val result = containerController.getOrdersByContainerId(containerId)

        // Then
        val resultList = result.toList()
        assertEquals(2, resultList.size)
        assertEquals(order1.id, resultList[0].id)
        assertEquals(order1.purchaseNumber, resultList[0].purchaseNumber)
        assertEquals(order2.id, resultList[1].id)
        assertEquals(order2.purchaseNumber, resultList[1].purchaseNumber)
    }

    @Test
    fun `getOrdersByContainerId should return empty flow when no orders exist for container`() = runTest {
        // Given
        val containerId = "CONT-001"
        val emptyOrdersFlow: Flow<Order> = flowOf()

        coEvery { containerService.findOrdersByContainerId(containerId) }.returns(emptyOrdersFlow)

        // When
        val result = containerController.getOrdersByContainerId(containerId)

        // Then
        val resultList = result.toList()
        assertEquals(0, resultList.size)
    }

    @Test
    fun `getAllContainers should throw RuntimeException with custom message when service fails`() = runTest {
        // Given

        coEvery { containerService.findAllContainersForCurrentUser() } returns flow {
            throw RuntimeException("Error de base de datos al buscar contenedores")
        }

        // When & Then
        assertThrows<RuntimeException> {
            containerController.getAllContainers().toList()
        }
    }

    @Test
    fun `getOrdersByContainerId should throw RuntimeException with custom message when service fails`() = runTest {
        // Given
        val containerId = "CONT-001"

        coEvery { containerService.findOrdersByContainerId(containerId) } returns flow {
            throw RuntimeException("Error de base de datos al buscar órdenes")
        }

        // When & Then
        assertThrows<RuntimeException> {
            containerController.getOrdersByContainerId(containerId).toList()
        }
    }
}
