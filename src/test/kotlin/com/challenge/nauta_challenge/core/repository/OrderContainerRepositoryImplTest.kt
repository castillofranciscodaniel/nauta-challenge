package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.OrderContainerRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderContainerEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.*

@SpringBootTest
class OrderContainerRepositoryImplTest {

    private val orderContainerDao = mockk<OrderContainerDao>()
    private val orderContainerRepository = OrderContainerRepositoryImpl(orderContainerDao)

    @Test
    fun savesOrderContainerRelationshipSuccessfully() {
        val orderId = 1L
        val containerId = 2L
        val orderContainerEntity = OrderContainerEntity(orderId = orderId, containerId = containerId)

        every { orderContainerDao.save(orderContainerEntity) } returns Mono.just(orderContainerEntity)

        StepVerifier.create(orderContainerRepository.save(orderId, containerId))
            .assertNext { result ->
                assertEquals(orderId, result.orderId)
                assertEquals(containerId, result.containerId)
            }
            .verifyComplete()
    }

    @Test
    fun throwsExceptionWhenRelationshipNotSaved() {
        val orderId = 1L
        val containerId = 2L
        val orderContainerEntity = OrderContainerEntity(orderId = orderId, containerId = containerId)

        every { orderContainerDao.save(orderContainerEntity) } returns Mono.empty()

        StepVerifier.create(orderContainerRepository.save(orderId, containerId))
            .expectError(ModelNotSavedException::class.java)
            .verify()
    }

    @Test
    fun checksExistenceByOrderIdAndContainerId() {
        val orderId = 1L
        val containerId = 2L

        every { orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId) } returns Mono.just(true)

        StepVerifier.create(orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId))
            .assertNext { result ->
                assertTrue(result)
            }
            .verifyComplete()
    }

    @Test
    fun returnsFalseWhenRelationshipDoesNotExist() {
        val orderId = 1L
        val containerId = 2L

        every { orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId) } returns Mono.just(false)

        StepVerifier.create(orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId))
            .assertNext { result ->
                assertFalse(result)
            }
            .verifyComplete()
    }
}