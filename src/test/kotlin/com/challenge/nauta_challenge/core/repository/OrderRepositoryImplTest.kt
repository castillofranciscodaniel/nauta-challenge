package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.OrderRepositoryImpl
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@SpringBootTest
class OrderRepositoryImplTest {

    private val orderDao = mockk<OrderDao>()
    private val orderRepository: OrderRepository = OrderRepositoryImpl(orderDao)

    @Test
    fun savesOrderSuccessfully() = runTest {
        val order = Order(id = null, purchaseNumber = "PO123", bookingId = 1, invoices = emptyList())
        val orderEntity = OrderEntity(id = null, purchaseNumber = "PO123", bookingId = 1)

        every { orderDao.save(orderEntity) }.returns(Mono.just(orderEntity.copy(id = 1)))

        val result = orderRepository.save(order)

        assertEquals(1, result.id)
        assertEquals("PO123", result.purchaseNumber)
        assertEquals(1, result.bookingId)
    }

    @Test
    fun throwsExceptionWhenOrderNotSaved(): Unit = runTest {
        val order = Order(id = null, purchaseNumber = "PO123", bookingId = 1, invoices = emptyList())
        val orderEntity = OrderEntity(id = null, purchaseNumber = "PO123", bookingId = 1)

        every { orderDao.save(orderEntity) }.returns(Mono.empty())

        assertFailsWith<Exception>("Order not saved") {
            orderRepository.save(order)
        }
    }

    @Test
    fun findsOrderByPurchaseNumberAndBookingId() = runTest {
        val orderEntity = OrderEntity(id = 1, purchaseNumber = "PO123", bookingId = 1)

        every { orderDao.findByPurchaseNumberAndBookingId("PO123", 1) }
            .returns(Mono.just(orderEntity))

        val result = orderRepository.findByPurchaseNumberAndBookingId("PO123", 1)

        assertEquals(1, result?.id)
        assertEquals("PO123", result?.purchaseNumber)
        assertEquals(1, result?.bookingId)
    }

    @Test
    fun returnsNullWhenOrderNotFound() = runTest {
        every { orderDao.findByPurchaseNumberAndBookingId("PO123", 1) }.returns(Mono.empty())

        val result = orderRepository.findByPurchaseNumberAndBookingId("PO123", 1)

        assertNull(result)
    }

    @Test
    fun `findAllByBookingId returns flow of orders`() = runTest {
        // Arrange
        val bookingId = 1L
        val orderEntity1 = OrderEntity(id = 1, purchaseNumber = "PO-123", bookingId = bookingId)
        val orderEntity2 = OrderEntity(id = 2, purchaseNumber = "PO-456", bookingId = bookingId)

        every { orderDao.findAllByBookingId(bookingId) } returns
                Flux.just(orderEntity1, orderEntity2)

        // Act
        val result = orderRepository.findAllByBookingId(bookingId)
        val orders = result.toList()

        // Assert
        assertEquals(2, orders.size)
        assertEquals(1L, orders[0].id)
        assertEquals("PO-123", orders[0].purchaseNumber)
        assertEquals(bookingId, orders[0].bookingId)
        assertEquals(2L, orders[1].id)
        assertEquals("PO-456", orders[1].purchaseNumber)
        assertEquals(bookingId, orders[1].bookingId)
    }

    @Test
    fun `findAllByBookingId returns empty flow when no orders found`() = runTest {
        // Arrange
        val bookingId = 1L

        every { orderDao.findAllByBookingId(bookingId) } returns
                Flux.empty()

        // Act
        val result = orderRepository.findAllByBookingId(bookingId)
        val orders = result.toList()

        // Assert
        assertEquals(0, orders.size)
    }

}