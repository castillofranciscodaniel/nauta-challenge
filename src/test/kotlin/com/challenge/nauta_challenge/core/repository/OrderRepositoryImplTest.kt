package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.OrderRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
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
import kotlin.test.assertTrue

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

    @Test
    fun `throws RepositoryException when findAllByUserId fails`() = runTest {
        // Arrange
        val userId = 1L

        every {
            orderDao.findAllByUserId(userId)
        } throws RuntimeException("Database error")

        // Act & Assert
        val exception = assertFailsWith<RepositoryException> {
            orderRepository.findAllByUserId(userId).toList()
        }

        assertTrue(exception.message!!.contains("Error retrieving orders for user"))
    }

    @Test
    fun `throws RepositoryException when findOrdersByContainerIdAndUserId fails`() = runTest {
        // Arrange
        val containerId = "CONT-001"
        val userId = 1L

        every {
            orderDao.findOrdersByContainerIdAndUserId(containerId, userId)
        } throws RuntimeException("Database error")

        // Act & Assert
        val exception = assertFailsWith<RepositoryException> {
            orderRepository.findOrdersByContainerIdAndUserId(containerId, userId).toList()
        }

        assertTrue(exception.message!!.contains("Error retrieving orders by container"))
    }

    @Test
    fun `throws RepositoryException when findByPurchaseNumberAndBookingId fails`() = runTest {
        // Arrange
        val purchaseNumber = "PO-001"
        val bookingId = 1L

        every {
            orderDao.findByPurchaseNumberAndBookingId(purchaseNumber, bookingId)
        } throws RuntimeException("Database error")

        // Act & Assert
        val exception = assertFailsWith<RepositoryException> {
            orderRepository.findByPurchaseNumberAndBookingId(purchaseNumber, bookingId)
        }

        assertTrue(exception.message!!.contains("Error finding order"))
    }

    @Test
    fun `findAllByUserId returns orders successfully`() = runTest {
        // Arrange
        val userId = 1L
        val orderEntities = listOf(
            OrderEntity(id = 1, purchaseNumber = "PO-001", bookingId = 1),
            OrderEntity(id = 2, purchaseNumber = "PO-002", bookingId = 2)
        )

        every { orderDao.findAllByUserId(userId) } returns Flux.fromIterable(orderEntities)

        // Act
        val result = orderRepository.findAllByUserId(userId).toList()

        // Assert
        assertEquals(2, result.size)
        assertEquals(1, result[0].id)
        assertEquals("PO-001", result[0].purchaseNumber)
        assertEquals(1, result[0].bookingId)
        assertEquals(2, result[1].id)
        assertEquals("PO-002", result[1].purchaseNumber)
        assertEquals(2, result[1].bookingId)
    }

    @Test
    fun `findAllByUserId returns empty list when no orders exist`() = runTest {
        // Arrange
        val userId = 1L

        every { orderDao.findAllByUserId(userId) } returns Flux.empty()

        // Act
        val result = orderRepository.findAllByUserId(userId).toList()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `throws ModelNotSavedException when save throws database exception`() = runTest {
        // Arrange
        val order = Order(id = null, purchaseNumber = "PO123", bookingId = 1, invoices = emptyList())
        val orderEntity = OrderEntity(id = null, purchaseNumber = "PO123", bookingId = 1)

        every { orderDao.save(orderEntity) } throws RuntimeException("Database connection failed")

        // Act & Assert
        val exception = assertFailsWith<ModelNotSavedException> {
            orderRepository.save(order)
        }

        assertTrue(exception.message!!.contains("Order not saved"))
    }
}