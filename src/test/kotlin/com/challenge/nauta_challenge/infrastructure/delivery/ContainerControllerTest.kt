package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.ContainerService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import kotlin.test.assertEquals

@SpringBootTest
class ContainerControllerTest {

    private val containerService = mockk<ContainerService>()
    private val containerController = ContainerController(containerService)

    @Test
    fun `getAllContainers should return flow of containers from service`() {
        // Given
        val container1 = Container(id = 1L, containerNumber = "CONT-001", bookingId = 100L)
        val container2 = Container(id = 2L, containerNumber = "CONT-002", bookingId = 100L)

        every { containerService.findAllContainersForCurrentUser() }.returns(Flux.just(container1, container2))

        // When & Then
        StepVerifier.create(containerController.getAllContainers())
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
    fun `getAllContainers should return empty flow when no containers exist`() {
        // Given
        every { containerService.findAllContainersForCurrentUser() }.returns(Flux.empty())

        // When & Then
        StepVerifier.create(containerController.getAllContainers())
            .verifyComplete()
    }

    @Test
    fun `getOrdersByContainerId should return flow of orders for specific container`() {
        // Given
        val containerId = "CONT-001"
        val order1 = Order(id = 1L, purchaseNumber = "PO-001", bookingId = 100L)
        val order2 = Order(id = 2L, purchaseNumber = "PO-002", bookingId = 100L)

        every { containerService.findOrdersByContainerId(containerId) }.returns(Flux.just(order1, order2))

        // When & Then
        StepVerifier.create(containerController.getOrdersByContainerId(containerId))
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
    fun `getOrdersByContainerId should return empty flow when no orders exist for container`() {
        // Given
        val containerId = "CONT-001"

        every { containerService.findOrdersByContainerId(containerId) }.returns(Flux.empty())

        // When & Then
        StepVerifier.create(containerController.getOrdersByContainerId(containerId))
            .verifyComplete()
    }
}
