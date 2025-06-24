package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

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
    fun returnsExistingContainerIfAlreadyStored() {
        // Arrange
        val bookingId = 1L
        val containerNumber = "CONT12345"
        val container = Container(
            id = null,
            containerNumber = containerNumber,
            bookingId = null
        )
        val existingContainer = Container(
            id = 1L,
            containerNumber = containerNumber,
            bookingId = bookingId
        )

        every { containerRepository.findByContainerNumberAndBookingId(containerNumber, bookingId) } returns Mono.just(existingContainer)

        // Act & Assert
        StepVerifier.create(containerService.saveContainersForBooking(listOf(container), bookingId))
            .assertNext { containers ->
                assert(containers.size == 1)
                assert(containers[0].id == existingContainer.id)
                assert(containers[0].containerNumber == existingContainer.containerNumber)
                assert(containers[0].bookingId == existingContainer.bookingId)
            }
            .verifyComplete()
    }

    @Test
    fun savesContainerWhenNotFound() {
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

        every { containerRepository.findByContainerNumberAndBookingId(containerNumber, bookingId) } returns Mono.empty()
        every { containerRepository.save(container.copy(bookingId = bookingId)) } returns Mono.just(savedContainer)

        // Act & Assert
        StepVerifier.create(containerService.saveContainersForBooking(listOf(container), bookingId))
            .assertNext { containers ->
                assert(containers.size == 1)
                assert(containers[0].id == savedContainer.id)
                assert(containers[0].containerNumber == savedContainer.containerNumber)
                assert(containers[0].bookingId == savedContainer.bookingId)
            }
            .verifyComplete()
    }

    @Test
    fun findsAllContainersForCurrentUser() {
        // Arrange
        val userId = 1L
        val user = User(id = userId, email = "test@example.com", password = "password")
        val container1 = Container(id = 101L, containerNumber = "CONT-001", bookingId = 100L)
        val container2 = Container(id = 102L, containerNumber = "CONT-002", bookingId = 100L)

        every { userLoggedService.getCurrentUserId() } returns Mono.just(user)
        every { containerRepository.findAllByUserId(userId) } returns Flux.just(container1, container2)

        // Act & Assert
        StepVerifier.create(containerService.findAllContainersForCurrentUser())
            .expectNext(container1)
            .expectNext(container2)
            .verifyComplete()
    }

    @Test
    fun findsOrdersByContainerId() {
        // Arrange
        val containerId = "CONT-001"
        val userId = 1L
        val user = User(id = userId, email = "user@example.com", password = "password")

        val order1 = Order(id = 1L, purchaseNumber = "PO-001", bookingId = 100L, invoices = emptyList())
        val order2 = Order(id = 2L, purchaseNumber = "PO-002", bookingId = 100L, invoices = emptyList())

        every { userLoggedService.getCurrentUserId() } returns Mono.just(user)
        every { orderRepository.findOrdersByContainerIdAndUserId(containerId, userId) } returns
                Flux.just(order1, order2)

        // Act & Assert
        StepVerifier.create(containerService.findOrdersByContainerId(containerId))
            .expectNext(order1)
            .expectNext(order2)
            .verifyComplete()
    }
}