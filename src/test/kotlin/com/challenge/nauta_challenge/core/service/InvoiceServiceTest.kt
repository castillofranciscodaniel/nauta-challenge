package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals

@SpringBootTest
class InvoiceServiceTest {

    private val invoiceRepository = mockk<InvoiceRepository>()
    private val invoiceService = InvoiceService(invoiceRepository)

    @Test
    fun savesInvoiceWhenNotFound() {
        // Arrange
        val orderId = 1L
        val invoice = Invoice(
            id = null,
            invoiceNumber = "INV-001",
            orderId = null
        )
        val savedInvoice = Invoice(
            id = 1L,
            invoiceNumber = "INV-001",
            orderId = orderId
        )

        every { invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId) } returns Mono.empty()
        every { invoiceRepository.save(invoice.copy(orderId = orderId)) } returns Mono.just(savedInvoice)

        // Act & Assert
        StepVerifier.create(invoiceService.saveInvoicesForOrder(listOf(invoice), orderId))
            .assertNext { result ->
                assertEquals(1, result.size)
                assertEquals(savedInvoice.id, result[0].id)
                assertEquals(savedInvoice.invoiceNumber, result[0].invoiceNumber)
                assertEquals(orderId, result[0].orderId)
            }
            .verifyComplete()
    }

    @Test
    fun returnsExistingInvoiceIfAlreadyStored() {
        // Arrange
        val orderId = 1L
        val invoice = Invoice(
            id = null,
            invoiceNumber = "INV-001",
            orderId = null
        )
        val existingInvoice = Invoice(
            id = 1L,
            invoiceNumber = "INV-001",
            orderId = orderId
        )

        every { invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId) } returns Mono.just(existingInvoice)

        // Act & Assert
        StepVerifier.create(invoiceService.saveInvoicesForOrder(listOf(invoice), orderId))
            .assertNext { result ->
                assertEquals(1, result.size)
                assertEquals(existingInvoice.id, result[0].id)
                assertEquals(existingInvoice.invoiceNumber, result[0].invoiceNumber)
                assertEquals(orderId, result[0].orderId)
            }
            .verifyComplete()
    }

    @Test
    fun findsAllInvoicesByOrderId() {
        // Arrange
        val orderId = 1L
        val invoice1 = Invoice(id = 1L, invoiceNumber = "INV-001", orderId = orderId)
        val invoice2 = Invoice(id = 2L, invoiceNumber = "INV-002", orderId = orderId)

        every { invoiceRepository.findAllByOrderId(orderId) } returns Flux.just(invoice1, invoice2)

        // Act & Assert
        StepVerifier.create(invoiceService.findAllByOrderId(orderId))
            .expectNext(invoice1)
            .expectNext(invoice2)
            .verifyComplete()
    }
}