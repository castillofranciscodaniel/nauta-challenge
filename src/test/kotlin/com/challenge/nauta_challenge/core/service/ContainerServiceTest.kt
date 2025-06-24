package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest
class ContainerServiceTest {

    private val containerRepository = mockk<ContainerRepository>(relaxed = true)
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
        // No debería llamar a save, pero lo mockeamos para evitar errores del flujo reactivo
        every { containerRepository.save(any()) } returns Mono.just(existingContainer)

        // Act & Assert
        StepVerifier.create(containerService.saveContainersForBooking(listOf(container), bookingId))
            .assertNext { containers ->
                assert(containers.size == 1)
                assert(containers[0].id == existingContainer.id)
                assert(containers[0].containerNumber == existingContainer.containerNumber)
                assert(containers[0].bookingId == existingContainer.bookingId)
            }
            .verifyComplete()

        // Verificar que save no fue llamado cuando ya existe el contenedor
        verify(exactly = 0) { containerRepository.save(any()) }
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
        val invoice1 = Invoice(id = 10L, invoiceNumber = "INV-001", orderId = order1.id)

        every { userLoggedService.getCurrentUserId() } returns Mono.just(user)
        every { orderRepository.findOrdersByContainerIdAndUserId(containerId, userId) } returns
                Flux.just(order1, order2)
        every { invoiceService.findAllByOrderId(1L) } returns Flux.just(invoice1)
        every { invoiceService.findAllByOrderId(2L) } returns Flux.empty()

        // Act & Assert
        StepVerifier.create(containerService.findOrdersByContainerId(containerId))
            .assertNext { result ->
                assert(result.id == order1.id)
                assert(result.purchaseNumber == order1.purchaseNumber)
                assert(result.invoices.size == 1)
                assert(result.invoices[0].id == invoice1.id)
            }
            .assertNext { result ->
                assert(result.id == order2.id)
                assert(result.purchaseNumber == order2.purchaseNumber)
                assert(result.invoices.isEmpty())
            }
            .verifyComplete()
    }
}