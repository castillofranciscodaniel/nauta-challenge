package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.OrderService
import com.challenge.nauta_challenge.infrastructure.delivery.OrderController
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class OrderControllerTest {

    private var orderService = mockk<OrderService>()

    private var orderController = OrderController(orderService)

    @Test
    fun `getAllOrders should return flow of orders from service`() = runTest {
        // Given
        val order1 = Order(id = 1L, purchaseNumber = "PO-001", bookingId = 100L, invoices = listOf())
        val order2 = Order(id = 2L, purchaseNumber = "PO-002", bookingId = 100L, invoices = listOf())
        val ordersFlow: Flow<Order> = flowOf(order1, order2)

        coEvery { orderService.findAllOrdersForCurrentUser() }.returns(ordersFlow)

        // When
        val result = orderController.getAllOrders()

        // Then
        val resultList = result.toList()
        assertEquals(2, resultList.size)
        assertEquals(order1.id, resultList[0].id)
        assertEquals(order2.id, resultList[1].id)
    }

    @Test
    fun `getAllOrders should return empty flow when no orders exist`() = runTest {
        // Given
        val emptyOrdersFlow: Flow<Order> = flowOf()

        coEvery { orderService.findAllOrdersForCurrentUser() }.returns(emptyOrdersFlow)

        // When
        val result = orderController.getAllOrders()

        // Then
        val resultList = result.toList()
        assertEquals(0, resultList.size, "Should return empty list when no orders exist")
    }

    @Test
    fun `getContainersByOrderId should return containers for specific order`() = runTest {
        // Given
        val purchaseNumber = "123L"
        val container1 = Container(id = 1L, containerNumber = "CONT-001", bookingId = 100L)
        val container2 = Container(id = 2L, containerNumber = "CONT-002", bookingId = 100L)
        val containersFlow: Flow<Container> = flowOf(container1, container2)

        coEvery { orderService.findContainersByOrderId(purchaseNumber) }.returns(containersFlow)

        // When
        val result = orderController.getContainersByOrderId(purchaseNumber)

        // Then
        val resultList = result.toList()
        assertEquals(2, resultList.size)
        assertEquals(container1.id, resultList[0].id)
        assertEquals(container1.containerNumber, resultList[0].containerNumber)
        assertEquals(container2.id, resultList[1].id)
        assertEquals(container2.containerNumber, resultList[1].containerNumber)
    }

    @Test
    fun `getContainersByOrderId should return empty flow when no containers are associated with order`() = runTest {
        // Given
        val purchaseNumber = "123L"
        val emptyContainersFlow: Flow<Container> = flowOf()

        coEvery { orderService.findContainersByOrderId(purchaseNumber) }.returns(emptyContainersFlow)

        // When
        val result = orderController.getContainersByOrderId(purchaseNumber)

        // Then
        val resultList = result.toList()
        assertEquals(0, resultList.size)
    }

}
