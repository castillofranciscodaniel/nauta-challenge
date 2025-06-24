package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.*
import com.challenge.nauta_challenge.core.repository.BookingRepository
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
import kotlin.test.assertEquals

@SpringBootTest
class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val invoiceService = mockk<InvoiceService>()
    private val bookingRepository = mockk<BookingRepository>()
    private val userLoggedService = mockk<UserLoggedService>()
    private val containerRepository = mockk<ContainerRepository>()
    private val orderService = OrderService(
        orderRepository,
        invoiceService,
        bookingRepository,
        userLoggedService,
        containerRepository
    )

    @Test
    fun returnsExistingOrderIfAlreadyStored() {
        // Arrange
        val bookingId = 1L
        val purchaseNumber = "PO-123"
        val invoice = Invoice(id = null, invoiceNumber = "INV-001", orderId = null)
        val order = Order(
            id = null,
            purchaseNumber = purchaseNumber,
            bookingId = null,
            invoices = listOf(invoice)
        )
        val existingOrder = Order(
            id = 10L,
            purchaseNumber = purchaseNumber,
            bookingId = bookingId,
            invoices = emptyList()
        )
        val savedInvoice = Invoice(
            id = 20L,
            invoiceNumber = "INV-001",
            orderId = 10L
        )

        every { orderRepository.findByPurchaseNumberAndBookingId(purchaseNumber, bookingId) } returns Mono.just(
            existingOrder
        )
        every { invoiceService.saveInvoicesForOrder(listOf(invoice), 10L) } returns Mono.just(listOf(savedInvoice))

        // Act & Assert
        StepVerifier.create(orderService.saveOrdersForBooking(listOf(order), bookingId))
            .assertNext { result ->
                assertEquals(1, result.size)
                assertEquals(10L, result[0].id)
                assertEquals(purchaseNumber, result[0].purchaseNumber)
                assertEquals(bookingId, result[0].bookingId)
                assertEquals(1, result[0].invoices.size)
                assertEquals(20L, result[0].invoices[0].id)
            }
            .verifyComplete()
    }

    @Test
    fun savesOrderWhenItDoesNotExist() {
        // Arrange
        val bookingId = 1L
        val purchaseNumber = "PO-123"
        val invoice = Invoice(id = null, invoiceNumber = "INV-001", orderId = null)
        val order = Order(
            id = null,
            purchaseNumber = purchaseNumber,
            bookingId = null,
            invoices = listOf(invoice)
        )
        val savedOrder = Order(
            id = 10L,
            purchaseNumber = purchaseNumber,
            bookingId = bookingId,
            invoices = emptyList()
        )
        val savedInvoice = Invoice(
            id = 20L,
            invoiceNumber = "INV-001",
            orderId = 10L
        )

        every { orderRepository.findByPurchaseNumberAndBookingId(purchaseNumber, bookingId) } returns Mono.empty()
        every { orderRepository.save(order.copy(bookingId = bookingId)) } returns Mono.just(savedOrder)
        every { invoiceService.saveInvoicesForOrder(listOf(invoice), 10L) } returns Mono.just(listOf(savedInvoice))

        // Act & Assert
        StepVerifier.create(orderService.saveOrdersForBooking(listOf(order), bookingId))
            .assertNext { result ->
                assertEquals(1, result.size)
                assertEquals(10L, result[0].id)
                assertEquals(purchaseNumber, result[0].purchaseNumber)
                assertEquals(bookingId, result[0].bookingId)
                assertEquals(1, result[0].invoices.size)
                assertEquals(20L, result[0].invoices[0].id)
            }
            .verifyComplete()
    }

    @Test
    fun processesMultipleOrdersCorrectly() {
        // Arrange
        val bookingId = 1L
        val purchaseNumber1 = "PO-123"
        val purchaseNumber2 = "PO-456"

        val invoice1 = Invoice(id = null, invoiceNumber = "INV-001", orderId = null)
        val invoice2 = Invoice(id = null, invoiceNumber = "INV-002", orderId = null)

        val order1 = Order(
            id = null,
            purchaseNumber = purchaseNumber1,
            bookingId = null,
            invoices = listOf(invoice1)
        )
        val order2 = Order(
            id = null,
            purchaseNumber = purchaseNumber2,
            bookingId = null,
            invoices = listOf(invoice2)
        )

        val existingOrder = Order(
            id = 10L,
            purchaseNumber = purchaseNumber1,
            bookingId = bookingId,
            invoices = emptyList()
        )

        val savedOrder = Order(
            id = 20L,
            purchaseNumber = purchaseNumber2,
            bookingId = bookingId,
            invoices = emptyList()
        )

        val savedInvoice1 = Invoice(id = 30L, invoiceNumber = "INV-001", orderId = 10L)
        val savedInvoice2 = Invoice(id = 40L, invoiceNumber = "INV-002", orderId = 20L)

        every { orderRepository.findByPurchaseNumberAndBookingId(purchaseNumber1, bookingId) } returns Mono.just(
            existingOrder
        )
        every { orderRepository.findByPurchaseNumberAndBookingId(purchaseNumber2, bookingId) } returns Mono.empty()
        every { orderRepository.save(order2.copy(bookingId = bookingId)) } returns Mono.just(savedOrder)
        every { invoiceService.saveInvoicesForOrder(listOf(invoice1), 10L) } returns Mono.just(listOf(savedInvoice1))
        every { invoiceService.saveInvoicesForOrder(listOf(invoice2), 20L) } returns Mono.just(listOf(savedInvoice2))

        // Act & Assert
        StepVerifier.create(orderService.saveOrdersForBooking(listOf(order1, order2), bookingId))
            .assertNext { result ->
                assertEquals(2, result.size)
                assertEquals(10L, result[0].id)
                assertEquals(purchaseNumber1, result[0].purchaseNumber)
                assertEquals(bookingId, result[0].bookingId)
                assertEquals(1, result[0].invoices.size)
                assertEquals(30L, result[0].invoices[0].id)

                assertEquals(20L, result[1].id)
                assertEquals(purchaseNumber2, result[1].purchaseNumber)
                assertEquals(bookingId, result[1].bookingId)
                assertEquals(1, result[1].invoices.size)
                assertEquals(40L, result[1].invoices[0].id)
            }
            .verifyComplete()
    }

    @Test
    fun handlesEmptyListCorrectly() {
        // Arrange
        val bookingId = 1L

        // Act & Assert
        StepVerifier.create(orderService.saveOrdersForBooking(emptyList(), bookingId))
            .assertNext { result ->
                assertEquals(0, result.size)
            }
            .verifyComplete()

        verify(exactly = 0) { orderRepository.findByPurchaseNumberAndBookingId(any(), any()) }
        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { invoiceService.saveInvoicesForOrder(any(), any()) }
    }

    @Test
    fun `findAllOrdersForCurrentUser should return orders with invoices`() {
        // Arrange
        val userId = 1L
        val user = User(id = userId, email = "test@gmail.com")

        val order1 = Order(id = 201L, purchaseNumber = "PO-001", bookingId = 101L)
        val order2 = Order(id = 202L, purchaseNumber = "PO-002", bookingId = 102L)
        val invoice1 = Invoice(id = 301L, invoiceNumber = "INV-001", orderId = order1.id!!)
        val invoice2 = Invoice(id = 302L, invoiceNumber = "INV-002", orderId = order2.id!!)

        every { userLoggedService.getCurrentUserId() } returns Mono.just(user)
        every { orderRepository.findAllByUserId(userId) } returns Flux.just(order1, order2)
        every { invoiceService.findAllByOrderId(order1.id!!) } returns Flux.just(invoice1)
        every { invoiceService.findAllByOrderId(order2.id!!) } returns Flux.just(invoice2)

        // Act & Assert
        StepVerifier.create(orderService.findAllOrdersForCurrentUser())
            .assertNext { result ->
                assertEquals(order1.id, result.id)
                assertEquals(order1.purchaseNumber, result.purchaseNumber)
                assertEquals(1, result.invoices.size)
                assertEquals(invoice1.id, result.invoices[0].id)
            }
            .assertNext { result ->
                assertEquals(order2.id, result.id)
                assertEquals(order2.purchaseNumber, result.purchaseNumber)
                assertEquals(1, result.invoices.size)
                assertEquals(invoice2.id, result.invoices[0].id)
            }
            .verifyComplete()

        // Verify
        verify { userLoggedService.getCurrentUserId() }
        verify { orderRepository.findAllByUserId(userId) }
        verify { invoiceService.findAllByOrderId(order1.id!!) }
        verify { invoiceService.findAllByOrderId(order2.id!!) }
    }

    @Test
    fun `findAllOrdersForCurrentUser should return empty list when user has no orders`() {
        // Arrange
        val userId = 1L
        val user = User(id = userId, email = "test@gmail.com")

        every { userLoggedService.getCurrentUserId() } returns Mono.just(user)
        every { orderRepository.findAllByUserId(userId) } returns Flux.empty()

        // Act & Assert
        StepVerifier.create(orderService.findAllOrdersForCurrentUser())
            .verifyComplete()

        // Verify
        verify { userLoggedService.getCurrentUserId() }
        verify { orderRepository.findAllByUserId(userId) }
    }

    @Test
    fun `findContainersByOrderId should return containers for authorized order`() {
        // Arrange
        val userId = 1L
        val purchaseNumber = "PO-123"

        val user = User(id = userId, email = "user@example.com", password = "password")
        val bookingId = 42L

        val container1 = Container(id = 101L, containerNumber = "CONT-001", bookingId = bookingId)
        val container2 = Container(id = 102L, containerNumber = "CONT-002", bookingId = bookingId)

        // Mock current user
        every { userLoggedService.getCurrentUserId() } returns Mono.just(user)

        every { containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) } returns
                Flux.just(container1, container2)

        // Act & Assert
        StepVerifier.create(orderService.findContainersByOrderId(purchaseNumber))
            .assertNext { result ->
                assertEquals(container1.id, result.id)
                assertEquals(container1.containerNumber, result.containerNumber)
            }
            .assertNext { result ->
                assertEquals(container2.id, result.id)
                assertEquals(container2.containerNumber, result.containerNumber)
            }
            .verifyComplete()

        // Verify
        verify(exactly = 1) { userLoggedService.getCurrentUserId() }
        verify(exactly = 1) { containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) }
    }

    @Test
    fun `findContainersByOrderId should return empty flow when no containers are associated with order`() {
        // Arrange
        val userId = 1L
        val purchaseNumber = "PO-123"

        val user = User(id = userId, email = "user@example.com", password = "password")

        every { userLoggedService.getCurrentUserId() } returns Mono.just(user)

        every { containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) } returns
                Flux.empty()

        // Act & Assert
        StepVerifier.create(orderService.findContainersByOrderId(purchaseNumber))
            .verifyComplete()

        // Verify
        verify(exactly = 1) { userLoggedService.getCurrentUserId() }
        verify(exactly = 1) { containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) }
    }
}