package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.OrderRepositoryImpl
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class OrderRepositoryImplTest {

    private val orderDao = mockk<OrderDao>()
    private val orderRepository: OrderRepository = OrderRepositoryImpl(orderDao)

    @Test
    fun savesOrderSuccessfully() = runBlocking {
        val order = Order(id = null, purchaseNumber = "123", bookingId = 1, invoices = emptyList())
        val orderEntity = OrderEntity(id = null, purchaseNumber = "123", bookingId = 1)

        every { orderDao.save(orderEntity) }.returns(Mono.just(orderEntity.copy(id = 1)))

        val result = orderRepository.save(order)

        assertEquals(1, result.id)
        assertEquals("123", result.purchaseNumber)
        assertEquals(1, result.bookingId)
    }

    @Test
    fun throwsExceptionWhenOrderNotSaved(): Unit = runBlocking {
        val order = Order(id = null, purchaseNumber = "123", bookingId = 1, invoices = emptyList())

        val orderEntity = OrderEntity(id = null, purchaseNumber = "123", bookingId = 1)

        every { orderDao.save(orderEntity) }.returns(Mono.empty())

        assertFailsWith<Exception>("Order not saved") {
            orderRepository.save(order)
        }
    }

    @Test
    fun findsOrderByPurchaseNumberAndBookingId() = runBlocking {
        val orderEntity = OrderEntity(id = 1, purchaseNumber = "123", bookingId = 1)
        val order = orderEntity.toModel()

        every { orderDao.findByPurchaseNumberAndBookingId("123", 1) }
            .returns(Mono.just(orderEntity))

        val result = orderRepository.findByPurchaseNumberAndBookingId("123", 1)

        assertEquals(order, result)
    }

    @Test
    fun returnsNullWhenOrderNotFound() = runBlocking {
        every { orderDao.findByPurchaseNumberAndBookingId("123", 1) }.returns(Mono.empty())

        val result = orderRepository.findByPurchaseNumberAndBookingId("123", 1)

        assertEquals(null, result)
    }

    @Test
    fun `findAllByBookingId returns flow of orders`() = runBlocking {
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
    fun `findAllByBookingId returns empty flow when no orders found`() = runBlocking {
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