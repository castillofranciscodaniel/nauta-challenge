package com.challenge.nauta_challenge.infraestructure.delivery

import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.OrderService
import com.challenge.nauta_challenge.infrastructure.delivery.OrderController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class, SpringExtension::class)
class OrderControllerTest {

    @Mock
    private lateinit var orderService: OrderService

    @InjectMocks
    private lateinit var orderController: OrderController

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getAllOrders should return flow of orders from service`() = runTest {
        // Given
        val order1 = Order(id = 1L, purchaseNumber = "PO-001", bookingId = 100L, invoices = listOf())
        val order2 = Order(id = 2L, purchaseNumber = "PO-002", bookingId = 100L, invoices = listOf())
        val ordersFlow: Flow<Order> = flowOf(order1, order2)

        `when`(orderService.findAllOrdersForCurrentUser()).thenReturn(ordersFlow)

        // When
        val result = orderController.getAllOrders()

        // Then
        val resultList = result.toList()
        assertEquals(2, resultList.size)
        assertEquals(order1.id, resultList[0].id)
        assertEquals(order2.id, resultList[1].id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getAllOrders should return empty flow when no orders exist`() = runTest {
        // Given
        val emptyOrdersFlow: Flow<Order> = flowOf()

        `when`(orderService.findAllOrdersForCurrentUser()).thenReturn(emptyOrdersFlow)

        // When
        val result = orderController.getAllOrders()

        // Then
        val resultList = result.toList()
        assertEquals(0, resultList.size, "Should return empty list when no orders exist")
    }
}
