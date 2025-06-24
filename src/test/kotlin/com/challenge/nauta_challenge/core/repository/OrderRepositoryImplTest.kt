package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.OrderRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderEntity
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
class OrderRepositoryImplTest {

    private val orderDao = mockk<OrderDao>()
    private val orderRepository: OrderRepository = OrderRepositoryImpl(orderDao)

    @Test
    fun savesOrderSuccessfully() {
        val order = Order(id = null, purchaseNumber = "PO123", bookingId = 1, invoices = emptyList())
        val orderEntity = OrderEntity(id = null, purchaseNumber = "PO123", bookingId = 1)

        every { orderDao.save(orderEntity) }.returns(Mono.just(orderEntity.copy(id = 1)))

        StepVerifier.create(orderRepository.save(order))
            .assertNext { result ->
                assertEquals(1, result.id)
                assertEquals("PO123", result.purchaseNumber)
                assertEquals(1, result.bookingId)
            }
            .verifyComplete()
    }

    @Test
    fun throwsExceptionWhenOrderNotSaved() {
        val order = Order(id = null, purchaseNumber = "PO123", bookingId = 1, invoices = emptyList())
        val orderEntity = OrderEntity(id = null, purchaseNumber = "PO123", bookingId = 1)

        every { orderDao.save(orderEntity) }.returns(Mono.empty())

        StepVerifier.create(orderRepository.save(order))
            .expectError(ModelNotSavedException::class.java)
            .verify()
    }

    @Test
    fun findsOrderByPurchaseNumberAndBookingId() {
        val orderEntity = OrderEntity(id = 1, purchaseNumber = "PO123", bookingId = 1)

        every { orderDao.findByPurchaseNumberAndBookingId("PO123", 1) }
            .returns(Mono.just(orderEntity))

        StepVerifier.create(orderRepository.findByPurchaseNumberAndBookingId("PO123", 1))
            .assertNext { result ->
                assertEquals(1, result?.id)
                assertEquals("PO123", result?.purchaseNumber)
                assertEquals(1, result?.bookingId)
            }
            .verifyComplete()
    }

    @Test
    fun returnsEmptyMonoWhenOrderNotFound() {
        every { orderDao.findByPurchaseNumberAndBookingId("PO123", 1) }.returns(Mono.empty())

        StepVerifier.create(orderRepository.findByPurchaseNumberAndBookingId("PO123", 1))
            .verifyComplete()
    }

    @Test
    fun findsOrdersByContainerIdAndUserId() {
        // Arrange
        val containerId = "CONT-001"
        val userId = 1L
        val orderEntities = listOf(
            OrderEntity(id = 1, purchaseNumber = "PO-001", bookingId = 1),
            OrderEntity(id = 2, purchaseNumber = "PO-002", bookingId = 1)
        )

        every { orderDao.findOrdersByContainerIdAndUserId(containerId, userId) } returns
            Flux.fromIterable(orderEntities)

        // Act & Assert
        StepVerifier.create(orderRepository.findOrdersByContainerIdAndUserId(containerId, userId))
            .assertNext { result ->
                assertEquals(1, result.id)
                assertEquals("PO-001", result.purchaseNumber)
                assertEquals(1, result.bookingId)
            }
            .assertNext { result ->
                assertEquals(2, result.id)
                assertEquals("PO-002", result.purchaseNumber)
                assertEquals(1, result.bookingId)
            }
            .verifyComplete()
    }

    @Test
    fun findsOrdersByContainerIdAndUserId_EmptyResult() {
        // Arrange
        val containerId = "CONT-001"
        val userId = 1L

        every { orderDao.findOrdersByContainerIdAndUserId(containerId, userId) } returns Flux.empty()

        // Act & Assert
        StepVerifier.create(orderRepository.findOrdersByContainerIdAndUserId(containerId, userId))
            .verifyComplete()
    }

}