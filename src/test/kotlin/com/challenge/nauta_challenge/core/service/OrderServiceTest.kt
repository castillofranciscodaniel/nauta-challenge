package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.*
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
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
    fun returnsExistingOrderIfAlreadyStored(): Unit = runTest {
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

        coEvery { orderRepository.findByPurchaseNumberAndBookingId(purchaseNumber, bookingId) } returns existingOrder
        coEvery { invoiceService.saveInvoicesForOrder(listOf(invoice), 10L) } returns listOf(savedInvoice)

        // Act
        val result = orderService.saveOrdersForBooking(listOf(order), bookingId)

        // Assert
        assertEquals(1, result.size)
        assertEquals(10L, result[0].id)
        assertEquals(purchaseNumber, result[0].purchaseNumber)
        assertEquals(bookingId, result[0].bookingId)
        assertEquals(1, result[0].invoices.size)
        assertEquals(20L, result[0].invoices[0].id)
        assertEquals("INV-001", result[0].invoices[0].invoiceNumber)
        coVerify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun savesOrderWhenItDoesNotExist(): Unit = runTest {
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

        coEvery { orderRepository.findByPurchaseNumberAndBookingId(purchaseNumber, bookingId) } returns null
        coEvery { orderRepository.save(order.copy(bookingId = bookingId)) } returns savedOrder
        coEvery { invoiceService.saveInvoicesForOrder(listOf(invoice), 10L) } returns listOf(savedInvoice)

        // Act
        val result = orderService.saveOrdersForBooking(listOf(order), bookingId)

        // Assert
        assertEquals(1, result.size)
        assertEquals(10L, result[0].id)
        assertEquals(purchaseNumber, result[0].purchaseNumber)
        assertEquals(bookingId, result[0].bookingId)
        assertEquals(1, result[0].invoices.size)
        assertEquals(20L, result[0].invoices[0].id)
        assertEquals("INV-001", result[0].invoices[0].invoiceNumber)
        coVerify(exactly = 1) { orderRepository.save(order.copy(bookingId = bookingId)) }
    }

    @Test
    fun processesMultipleOrdersCorrectly(): Unit = runTest {
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

        coEvery { orderRepository.findByPurchaseNumberAndBookingId(purchaseNumber1, bookingId) } returns existingOrder
        coEvery { orderRepository.findByPurchaseNumberAndBookingId(purchaseNumber2, bookingId) } returns null
        coEvery { orderRepository.save(order2.copy(bookingId = bookingId)) } returns savedOrder
        coEvery { invoiceService.saveInvoicesForOrder(listOf(invoice1), 10L) } returns listOf(savedInvoice1)
        coEvery { invoiceService.saveInvoicesForOrder(listOf(invoice2), 20L) } returns listOf(savedInvoice2)

        // Act
        val result = orderService.saveOrdersForBooking(listOf(order1, order2), bookingId)

        // Assert
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

    @Test
    fun handlesEmptyListCorrectly(): Unit = runTest {
        // Arrange
        val bookingId = 1L

        // Act
        val result = orderService.saveOrdersForBooking(emptyList(), bookingId)

        // Assert
        assertEquals(0, result.size)
        coVerify(exactly = 0) { orderRepository.findByPurchaseNumberAndBookingId(any(), any()) }
        coVerify(exactly = 0) { orderRepository.save(any()) }
        coVerify(exactly = 0) { invoiceService.saveInvoicesForOrder(any(), any()) }
    }

    @Test
    fun `findAllOrdersForCurrentUser should return orders with invoices`(): Unit = runTest {
        // Arrange
        val userId = 1L
        val user = User(id = userId, email = "test@gmail.com")

        val order1 = Order(id = 201L, purchaseNumber = "PO-001", bookingId = 101L)
        val order2 = Order(id = 202L, purchaseNumber = "PO-002", bookingId = 102L)
        val invoice1 = Invoice(id = 301L, invoiceNumber = "INV-001", orderId = order1.id!!)
        val invoice2 = Invoice(id = 302L, invoiceNumber = "INV-002", orderId = order2.id!!)

        coEvery { userLoggedService.getCurrentUserId() } returns user
        coEvery { orderRepository.findAllByUserId(userId) } returns flowOf(order1, order2)
        coEvery { invoiceService.findAllByOrderId(order1.id!!) } returns flowOf(invoice1)
        coEvery { invoiceService.findAllByOrderId(order2.id!!) } returns flowOf(invoice2)

        // Act
        val result = orderService.findAllOrdersForCurrentUser().toList()

        // Assert
        assertEquals(2, result.size)
        assertEquals(order1.id, result[0].id)
        assertEquals(order1.purchaseNumber, result[0].purchaseNumber)
        assertEquals(1, result[0].invoices.size)
        assertEquals(invoice1.id, result[0].invoices[0].id)

        assertEquals(order2.id, result[1].id)
        assertEquals(order2.purchaseNumber, result[1].purchaseNumber)
        assertEquals(1, result[1].invoices.size)
        assertEquals(invoice2.id, result[1].invoices[0].id)

        // Verify
        coVerify { userLoggedService.getCurrentUserId() }
        coVerify { orderRepository.findAllByUserId(userId) }
        coVerify { invoiceService.findAllByOrderId(order1.id!!) }
        coVerify { invoiceService.findAllByOrderId(order2.id!!) }
    }

    @Test
    fun `findAllOrdersForCurrentUser should return empty list when user has no orders`(): Unit = runTest {
        // Arrange
        val userId = 1L
        val user = User(id = userId, email = "test@gmail.com")

        coEvery { userLoggedService.getCurrentUserId() } returns user
        coEvery { orderRepository.findAllByUserId(userId) } returns flowOf()

        // Act
        val result = orderService.findAllOrdersForCurrentUser().toList()

        // Assert
        assertEquals(0, result.size)

        // Verify
        coVerify { userLoggedService.getCurrentUserId() }
        coVerify { orderRepository.findAllByUserId(userId) }
    }

    @Test
    fun `findContainersByOrderId should return containers for authorized order`() = runTest {
        // Arrange
        val userId = 1L
        val purchaseNumber = "PO-123"

        val user = User(id = userId, email = "user@example.com", password = "password")
        val bookingId = 42L

        val container1 = Container(id = 101L, containerNumber = "CONT-001", bookingId = bookingId)
        val container2 = Container(id = 102L, containerNumber = "CONT-002", bookingId = bookingId)

        // Mock current user
        coEvery { userLoggedService.getCurrentUserId() } returns user

        coEvery { containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) } returns
                flowOf(container1, container2)

        // Act
        val result = orderService.findContainersByOrderId(purchaseNumber).toList()

        // Assert
        assertEquals(2, result.size)
        assertEquals(container1.id, result[0].id)
        assertEquals(container1.containerNumber, result[0].containerNumber)
        assertEquals(container2.id, result[1].id)
        assertEquals(container2.containerNumber, result[1].containerNumber)

        // Verify
        coVerify(exactly = 1) { userLoggedService.getCurrentUserId() }
        coVerify(exactly = 1) { containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) }
    }


    @Test
    fun `findContainersByOrderId should return empty flow when no containers are associated with order`() = runTest {
        // Arrange
        val userId = 1L
        val purchaseNumber = "PO-123"

        val user = User(id = userId, email = "user@example.com", password = "password")

        // Mock current user
        coEvery { userLoggedService.getCurrentUserId() } returns user

        coEvery { containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) } returns
                flowOf()

        // Act
        val result = orderService.findContainersByOrderId(purchaseNumber).toList()

        // Assert
        assertEquals(0, result.size)

        // Verify
        coVerify(exactly = 1) { userLoggedService.getCurrentUserId() }
        coVerify(exactly = 1) { containerRepository.findContainersByPurchaseNumberAndUserId(purchaseNumber, userId) }
    }
}