package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.OrderService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import kotlin.test.assertEquals

@SpringBootTest
class OrderControllerTest {

    private var orderService = mockk<OrderService>()

    private var orderController = OrderController(orderService)

    @Test
    fun `getAllOrders should return flow of orders from service`() {
        // Given
        val order1 = Order(id = 1L, purchaseNumber = "PO-001", bookingId = 100L, invoices = listOf())
        val order2 = Order(id = 2L, purchaseNumber = "PO-002", bookingId = 100L, invoices = listOf())

        every { orderService.findAllOrdersForCurrentUser() }.returns(Flux.just(order1, order2))

        // When & Then
        StepVerifier.create(orderController.getAllOrders())
            .assertNext { order ->
                assertEquals(order1.id, order.id)
                assertEquals(order1.purchaseNumber, order.purchaseNumber)
            }
            .assertNext { order ->
                assertEquals(order2.id, order.id)
                assertEquals(order2.purchaseNumber, order.purchaseNumber)
            }
            .verifyComplete()
    }

    @Test
    fun `getAllOrders should return empty flow when no orders exist`() {
        // Given
        every { orderService.findAllOrdersForCurrentUser() }.returns(Flux.empty())

        // When & Then
        StepVerifier.create(orderController.getAllOrders())
            .verifyComplete()
    }

    @Test
    fun `getContainersByOrderId should return containers for specific order`() {
        // Given
        val purchaseNumber = "123L"
        val container1 = Container(id = 1L, containerNumber = "CONT-001", bookingId = 100L)
        val container2 = Container(id = 2L, containerNumber = "CONT-002", bookingId = 100L)

        every { orderService.findContainersByOrderId(purchaseNumber) }.returns(Flux.just(container1, container2))

        // When & Then
        StepVerifier.create(orderController.getContainersByOrderId(purchaseNumber))
            .assertNext { container ->
                assertEquals(container1.id, container.id)
                assertEquals(container1.containerNumber, container.containerNumber)
            }
            .assertNext { container ->
                assertEquals(container2.id, container.id)
                assertEquals(container2.containerNumber, container.containerNumber)
            }
            .verifyComplete()
    }

    @Test
    fun `getContainersByOrderId should return empty flow when no containers are associated with order`() {
        // Given
        val purchaseNumber = "123L"
        every { orderService.findContainersByOrderId(purchaseNumber) }.returns(Flux.empty())

        // When & Then
        StepVerifier.create(orderController.getContainersByOrderId(purchaseNumber))
            .verifyComplete()
    }
}
