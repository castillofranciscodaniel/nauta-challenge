package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val invoiceService = mockk<InvoiceService>()
    private val bookingRepository = mockk<BookingRepository>()
    private val userLoggedService = mockk<UserLoggedService>()
    private val orderService = OrderService(
        orderRepository,
        invoiceService,
        bookingRepository,
        userLoggedService
    )

    @Test
    fun devuelveOrdenExistenteSiYaEstáAlmacenada(): Unit = runBlocking {
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
    fun guardaOrderCuandoNoExiste(): Unit = runBlocking {
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
    fun procesaMultiplesOrdenesCorrectamente(): Unit = runBlocking {
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
    fun manejaListaVaciaCorrectamente(): Unit = runBlocking {
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
    fun `findAllOrdersForCurrentUser should return orders with invoices`(): Unit = runBlocking {
        // Arrange
        val userId = 1L
        val user = mockk<User>()
        val bookingRepository = mockk<BookingRepository>()
        val userLoggedService = mockk<UserLoggedService>()
        val orderServiceWithBookings = OrderService(
            orderRepository,
            invoiceService,
            bookingRepository,
            userLoggedService
        )

        val booking1 = mockk<Booking>()
        val booking2 = mockk<Booking>()
        val order1 = mockk<Order>()
        val order2 = mockk<Order>()
        val invoice1 = mockk<Invoice>()
        val invoice2 = mockk<Invoice>()

        coEvery { user.id } returns userId
        coEvery { userLoggedService.getCurrentUserId() } returns user

        coEvery { booking1.id } returns 101L
        coEvery { booking2.id } returns 102L
        coEvery { order1.id } returns 201L
        coEvery { order2.id } returns 202L

        coEvery { bookingRepository.findAllByUserId(userId) } returns flowOf(booking1, booking2)
        coEvery { orderRepository.findAllByBookingId(101L) } returns flowOf(order1)
        coEvery { orderRepository.findAllByBookingId(102L) } returns flowOf(order2)

        coEvery { invoiceService.findAllByOrderId(201L) } returns flowOf(invoice1)
        coEvery { invoiceService.findAllByOrderId(202L) } returns flowOf(invoice2)

        coEvery { order1.copy(invoices = any()) } returns order1
        coEvery { order2.copy(invoices = any()) } returns order2

        // Act
        val result = orderServiceWithBookings.findAllOrdersForCurrentUser().toList()

        // Assert
        assertEquals(2, result.size)
        coVerify { userLoggedService.getCurrentUserId() }
        coVerify { bookingRepository.findAllByUserId(userId) }
        coVerify { orderRepository.findAllByBookingId(101L) }
        coVerify { orderRepository.findAllByBookingId(102L) }
        coVerify { invoiceService.findAllByOrderId(201L) }
        coVerify { invoiceService.findAllByOrderId(202L) }
    }
}