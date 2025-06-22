package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.OrderContainerRepositoryImpl
import com.challenge.nauta_challenge.core.exception.NotFoundException
import com.challenge.nauta_challenge.infrastructure.repository.dao.OrderContainerDao
import com.challenge.nauta_challenge.infrastructure.repository.model.OrderContainerEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import kotlin.test.*

@SpringBootTest
class OrderContainerRepositoryImplTest {

    private val orderContainerDao = mockk<OrderContainerDao>()
    private val orderContainerRepository: OrderContainerRepository = OrderContainerRepositoryImpl(orderContainerDao)

    @Test
    fun guardaRelacionOrdenContenedorExitosamente() = runBlocking {
        val orderId = 1L
        val containerId = 2L
        val orderContainerEntity = OrderContainerEntity(orderId = orderId, containerId = containerId)

        every { orderContainerDao.save(orderContainerEntity) }.returns(Mono.just(orderContainerEntity))

        val resultado = orderContainerRepository.save(orderId, containerId)

        assertEquals(orderId, resultado.orderId)
        assertEquals(containerId, resultado.containerId)
    }

    @Test
    fun lanzaExcepcionCuandoNoSeGuardaRelacion() = runBlocking {
        val orderId = 1L
        val containerId = 2L
        val orderContainerEntity = OrderContainerEntity(orderId = orderId, containerId = containerId)

        every { orderContainerDao.save(orderContainerEntity) }.returns(Mono.empty())

        assertFailsWith<NotFoundException>("OrderContainer not saved") {
            orderContainerRepository.save(orderId, containerId)
        }
    }

    @Test
    fun verificaExistenciaDeRelacionCuandoExiste() = runBlocking {
        val orderId = 1L
        val containerId = 2L

        every { orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId) }.returns(Mono.just(true))

        val resultado = orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId)

        assertTrue(resultado)
    }

    @Test
    fun verificaExistenciaDeRelacionCuandoNoExiste() = runBlocking {
        val orderId = 1L
        val containerId = 2L

        every { orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId) }.returns(Mono.just(false))

        val resultado = orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId)

        assertFalse(resultado)
    }

    @Test
    fun devuelveFalseCuandoExistsByOrderIdAndContainerIdRetornaMonoEmpty() = runBlocking {
        val orderId = 1L
        val containerId = 2L

        every { orderContainerDao.existsByOrderIdAndContainerId(orderId, containerId) }.returns(Mono.empty())

        val resultado = orderContainerRepository.existsByOrderIdAndContainerId(orderId, containerId)

        assertFalse(resultado)
    }
}