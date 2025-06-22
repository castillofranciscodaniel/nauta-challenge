package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.InvoiceRepositoryImpl
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.infrastructure.repository.dao.InvoiceDao
import com.challenge.nauta_challenge.infrastructure.repository.model.InvoiceEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class InvoiceRepositoryImplTest {

    private val invoiceDao = mockk<InvoiceDao>()
    private val invoiceRepository: InvoiceRepository = InvoiceRepositoryImpl(invoiceDao)

    @Test
    fun guardaFacturaExitosamente() = runBlocking {
        val invoice = Invoice(id = null, invoiceNumber = "FAC123", orderId = 1)
        val invoiceEntity = InvoiceEntity.fromModel(invoice)

        every { invoiceDao.save(invoiceEntity) }.returns(Mono.just(invoiceEntity.copy(id = 1)))

        val resultado = invoiceRepository.save(invoice)

        assertEquals(1, resultado.id)
        assertEquals("FAC123", resultado.invoiceNumber)
        assertEquals(1, resultado.orderId)
    }

    @Test
    fun lanzaExcepcionCuandoNoSeGuardaFactura(): Unit = runBlocking {
        val invoice = Invoice(id = null, invoiceNumber = "FAC123", orderId = 1)
        val invoiceEntity = InvoiceEntity.fromModel(invoice)

        every { invoiceDao.save(invoiceEntity) }.returns(Mono.empty())

        assertFailsWith<Exception>("Invoice not saved") {
            invoiceRepository.save(invoice)
        }
    }
}