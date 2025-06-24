package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.OrderContainerRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderContainerEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import kotlin.test.*

@SpringBootTest
class OrderContainerRepositoryImplTest {

    private val orderContainerDao = mockk<OrderContainerDao>()
    private val orderContainerRepository: OrderContainerRepository = OrderContainerRepositoryImpl(orderContainerDao)

    @Test
    fun savesOrderContainerRelationshipSuccessfully() = runTest {
        val orderId = 1L
        val containerId = 2L
        val orderContainerEntity = OrderContainerEntity(orderId = orderId, containerId = containerId)

        every { orderContainerDao.save(orderContainerEntity) }.returns(Mono.just(orderContainerEntity))

        val result = orderContainerRepository.save(orderId, containerId)

        assertEquals(orderId, result.orderId)
        assertEquals(containerId, result.containerId)
    }

    @Test
    fun throwsExceptionWhenRelationshipNotSaved() = runTest {
        val orderId = 1L
        val containerId = 2L
        val orderContainerEntity = OrderContainerEntity(orderId = orderId, containerId = containerId)

        every { orderContainerDao.save(orderContainerEntity) }.returns(Mono.empty())

        assertFailsWith<ModelNotSavedException>("OrderContainer not saved") {
            orderContainerRepository.save(orderId, containerId)
        }
    }

    @Test
    fun verifiesRelationshipExistenceWhenItExists() = runTest {
        val orderId = 1L
        val containerId = 2L

        every { orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId) }.returns(Mono.just(true))

        val result = orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId)

        assertTrue(result)
    }

    @Test
    fun verifiesRelationshipExistenceWhenItDoesNotExist() = runTest {
        val orderId = 1L
        val containerId = 2L

        every { orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId) }.returns(Mono.just(false))

        val result = orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId)

        assertFalse(result)
    }

    @Test
    fun returnsFalseWhenExistsByOrderIdAndContainerIdReturnsMonoEmpty() = runTest {
        val orderId = 1L
        val containerId = 2L

        every { orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId) }.returns(Mono.empty())

        val result = orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId)

        assertFalse(result)
    }
}