package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.InvoiceRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.infrastructure.repository.dao.InvoiceDao
import com.challenge.nauta_challenge.infrastructure.repository.model.InvoiceEntity
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
class InvoiceRepositoryImplTest {

    private val invoiceDao = mockk<InvoiceDao>()
    private val invoiceRepository: InvoiceRepository = InvoiceRepositoryImpl(invoiceDao)

    @Test
    fun saveInvoiceSuccessfully() {
        val invoice = Invoice(id = null, invoiceNumber = "INV-001", orderId = 1)
        val invoiceEntity = InvoiceEntity(id = null, invoiceNumber = "INV-001", orderId = 1)

        every { invoiceDao.save(invoiceEntity) }.returns(Mono.just(invoiceEntity.copy(id = 1)))

        StepVerifier.create(invoiceRepository.save(invoice))
            .assertNext { result ->
                assertEquals(1, result.id)
                assertEquals("INV-001", result.invoiceNumber)
                assertEquals(1, result.orderId)
            }
            .verifyComplete()
    }

    @Test
    fun throwsExceptionWhenInvoiceNotSaved() {
        val invoice = Invoice(id = null, invoiceNumber = "INV-001", orderId = 1)
        val invoiceEntity = InvoiceEntity(id = null, invoiceNumber = "INV-001", orderId = 1)

        every { invoiceDao.save(invoiceEntity) }.returns(Mono.empty())

        StepVerifier.create(invoiceRepository.save(invoice))
            .expectError(ModelNotSavedException::class.java)
            .verify()
    }

    @Test
    fun `findAllByOrderId returns flow of invoices`() {
        // Arrange
        val orderId = 1L
        val invoiceEntity1 = InvoiceEntity(id = 1, invoiceNumber = "INV-001", orderId = orderId)
        val invoiceEntity2 = InvoiceEntity(id = 2, invoiceNumber = "INV-002", orderId = orderId)

        every { invoiceDao.findAllByOrderId(orderId) }.returns(Flux.just(invoiceEntity1, invoiceEntity2))

        // Act & Assert
        StepVerifier.create(invoiceRepository.findAllByOrderId(orderId))
            .assertNext { invoice ->
                assertEquals(1, invoice.id)
                assertEquals("INV-001", invoice.invoiceNumber)
                assertEquals(orderId, invoice.orderId)
            }
            .assertNext { invoice ->
                assertEquals(2, invoice.id)
                assertEquals("INV-002", invoice.invoiceNumber)
                assertEquals(orderId, invoice.orderId)
            }
            .verifyComplete()
    }

    @Test
    fun `findAllByOrderId returns empty flow when no invoices found`() {
        // Arrange
        val orderId = 1L

        every { invoiceDao.findAllByOrderId(orderId) }.returns(Flux.empty())

        // Act & Assert
        StepVerifier.create(invoiceRepository.findAllByOrderId(orderId))
            .verifyComplete()
    }

    @Test
    fun `findByInvoiceNumberAndOrderId returns invoice when found`() {
        // Arrange
        val invoiceNumber = "INV-001"
        val orderId = 1L
        val invoiceEntity = InvoiceEntity(
            id = 1,
            invoiceNumber = invoiceNumber,
            orderId = orderId
        )

        every { invoiceDao.findByInvoiceNumberAndOrderId(invoiceNumber, orderId) }
            .returns(Mono.just(invoiceEntity))

        // Act & Assert
        StepVerifier.create(invoiceRepository.findByInvoiceNumberAndOrderId(invoiceNumber, orderId))
            .assertNext { invoice ->
                assertEquals(1, invoice.id)
                assertEquals(invoiceNumber, invoice.invoiceNumber)
                assertEquals(orderId, invoice.orderId)
            }
            .verifyComplete()
    }

    @Test
    fun `findByInvoiceNumberAndOrderId returns empty when not found`() {
        // Arrange
        val invoiceNumber = "INV-001"
        val orderId = 1L

        every { invoiceDao.findByInvoiceNumberAndOrderId(invoiceNumber, orderId) }
            .returns(Mono.empty())

        // Act & Assert
        StepVerifier.create(invoiceRepository.findByInvoiceNumberAndOrderId(invoiceNumber, orderId))
            .verifyComplete()
    }
}