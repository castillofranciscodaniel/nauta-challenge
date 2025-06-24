package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.OrderRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
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

        assertFailsWith<ModelNotSavedException>("Order not saved") {
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
    fun findsOrdersByContainerIdAndUserId() = runTest {
        // Arrange
        val containerId = "CONT-001"
        val userId = 1L
        val orderEntities = listOf(
            OrderEntity(id = 1, purchaseNumber = "PO-001", bookingId = 1),
            OrderEntity(id = 2, purchaseNumber = "PO-002", bookingId = 1)
        )

        every { orderDao.findOrdersByContainerIdAndUserId(containerId, userId) } returns
            Flux.fromIterable(orderEntities)

        // Act
        val result = orderRepository.findOrdersByContainerIdAndUserId(containerId, userId).toList()

        // Assert
        assertEquals(2, result.size)
        assertEquals(1, result[0].id)
        assertEquals("PO-001", result[0].purchaseNumber)
        assertEquals(1, result[0].bookingId)
        assertEquals(2, result[1].id)
        assertEquals("PO-002", result[1].purchaseNumber)
        assertEquals(1, result[1].bookingId)
    }

    @Test
    fun findsOrdersByContainerIdAndUserId_EmptyResult() = runTest {
        // Arrange
        val containerId = "CONT-001"
        val userId = 1L

        every { orderDao.findOrdersByContainerIdAndUserId(containerId, userId) } returns Flux.empty()

        // Act
        val result = orderRepository.findOrdersByContainerIdAndUserId(containerId, userId).toList()

        // Assert
        assertEquals(0, result.size)
    }

}