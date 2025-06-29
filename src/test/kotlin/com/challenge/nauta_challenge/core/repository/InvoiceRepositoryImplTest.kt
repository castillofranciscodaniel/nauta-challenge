package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.InvoiceRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.infrastructure.repository.dao.InvoiceDao
import com.challenge.nauta_challenge.infrastructure.repository.model.InvoiceEntity
import io.mockk.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@SpringBootTest
class InvoiceRepositoryImplTest {

    private val invoiceDao = mockk<InvoiceDao>()
    private val invoiceRepository: InvoiceRepository = InvoiceRepositoryImpl(invoiceDao)

    @Test
    fun saveInvoiceSuccessfully() = runTest {
        val invoice = Invoice(id = null, invoiceNumber = "FAC123", orderId = 1)
        val invoiceEntity = InvoiceEntity.fromModel(invoice)

        every { invoiceDao.save(invoiceEntity) }.returns(Mono.just(invoiceEntity.copy(id = 1)))

        val result = invoiceRepository.save(invoice)

        assertEquals(1, result.id)
        assertEquals("FAC123", result.invoiceNumber)
        assertEquals(1, result.orderId)
    }

    @Test
    fun throwsExceptionWhenInvoiceNotSaved(): Unit = runTest {
        val invoice = Invoice(id = null, invoiceNumber = "FAC123", orderId = 1)
        val invoiceEntity = InvoiceEntity.fromModel(invoice)

        every { invoiceDao.save(invoiceEntity) }.returns(Mono.empty())

        assertFailsWith<ModelNotSavedException>("Invoice not saved") {
            invoiceRepository.save(invoice)
        }
    }

    @Test
    fun `findAllByOrderId returns flow of invoices`() = runTest {
        // Arrange
        val orderId = 1L
        val invoiceEntity1 = InvoiceEntity(id = 1, invoiceNumber = "INV-123", orderId = orderId)
        val invoiceEntity2 = InvoiceEntity(id = 2, invoiceNumber = "INV-456", orderId = orderId)

        every { invoiceDao.findAllByOrderId(orderId) } returns
                Flux.just(invoiceEntity1, invoiceEntity2)

        // Act
        val result = invoiceRepository.findAllByOrderId(orderId)
        val invoices = result.toList()

        // Assert
        assertEquals(2, invoices.size)
        assertEquals(1L, invoices[0].id)
        assertEquals("INV-123", invoices[0].invoiceNumber)
        assertEquals(orderId, invoices[0].orderId)
        assertEquals(2L, invoices[1].id)
        assertEquals("INV-456", invoices[1].invoiceNumber)
        assertEquals(orderId, invoices[1].orderId)
    }

    @Test
    fun `findAllByOrderId returns empty flow when no invoices found`() = runTest {
        // Arrange
        val orderId = 1L

        every { invoiceDao.findAllByOrderId(orderId) } returns
                Flux.empty()

        // Act
        val result = invoiceRepository.findAllByOrderId(orderId)
        val invoices = result.toList()

        // Assert
        assertEquals(0, invoices.size)
    }

    @Test
    fun `findByInvoiceNumberAndOrderId returns invoice when found`() = runTest {
        // Arrange
        val invoiceNumber = "INV-123"
        val orderId = 1L
        val invoiceEntity = InvoiceEntity(id = 1, invoiceNumber = invoiceNumber, orderId = orderId)

        every { invoiceDao.findByInvoiceNumberAndOrderId(invoiceNumber, orderId) } returns
                Mono.just(invoiceEntity)

        // Act
        val result = invoiceRepository.findByInvoiceNumberAndOrderId(invoiceNumber, orderId)

        // Assert
        assertEquals(1L, result?.id)
        assertEquals(invoiceNumber, result?.invoiceNumber)
        assertEquals(orderId, result?.orderId)
    }

    @Test
    fun `findByInvoiceNumberAndOrderId returns null when not found`() = runTest {
        // Arrange
        val invoiceNumber = "INV-123"
        val orderId = 1L

        every { invoiceDao.findByInvoiceNumberAndOrderId(invoiceNumber, orderId) } returns
                Mono.empty()

        // Act
        val result = invoiceRepository.findByInvoiceNumberAndOrderId(invoiceNumber, orderId)

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `throws RepositoryException when findAllByOrderId fails`() = runTest {
        // Arrange
        val orderId = 1L

        every {
            invoiceDao.findAllByOrderId(orderId)
        } throws RuntimeException("Database error")

        // Act & Assert
        val exception = assertFailsWith<RepositoryException> {
            invoiceRepository.findAllByOrderId(orderId).toList()
        }

        assertTrue(exception.message!!.contains("Error retrieving invoices for order"))
    }

    @Test
    fun `throws RepositoryException when findByInvoiceNumberAndOrderId fails`() = runTest {
        // Arrange
        val invoiceNumber = "INV-123"
        val orderId = 1L

        coEvery {
            invoiceDao.findByInvoiceNumberAndOrderId(invoiceNumber, orderId)
        } throws RuntimeException("Database error")

        // Act & Assert
        val exception = assertFailsWith<RepositoryException> {
            invoiceRepository.findByInvoiceNumberAndOrderId(invoiceNumber, orderId)
        }

        assertTrue(exception.message!!.contains("Error finding invoice"))
    }
}