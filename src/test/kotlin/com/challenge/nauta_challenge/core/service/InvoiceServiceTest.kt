package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class InvoiceServiceTest {

    private val invoiceRepository = mockk<InvoiceRepository>()
    private val invoiceService = InvoiceService(invoiceRepository)

    @Test
    fun saveInvvoice(): Unit = runTest {
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

        coEvery { invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId) } returns null
        coEvery { invoiceRepository.save(invoice.copy(orderId = orderId)) } returns savedInvoice

        // Act
        val result = invoiceService.saveInvoicesForOrder(listOf(invoice), orderId)

        // Assert
        assertEquals(1, result.size)
        assertEquals(savedInvoice.id, result[0].id)
        assertEquals(savedInvoice.invoiceNumber, result[0].invoiceNumber)
        assertEquals(orderId, result[0].orderId)
    }

    @Test
    fun guardaMultiplesFacturasCorrectamente(): Unit = runTest {
        // Arrange
        val orderId = 1L
        val invoice1 = Invoice(
            id = null,
            invoiceNumber = "INV-001",
            orderId = null
        )
        val invoice2 = Invoice(
            id = null,
            invoiceNumber = "INV-002",
            orderId = null
        )
        val savedInvoice1 = Invoice(
            id = 1L,
            invoiceNumber = "INV-001",
            orderId = orderId
        )
        val savedInvoice2 = Invoice(
            id = 2L,
            invoiceNumber = "INV-002",
            orderId = orderId
        )

        coEvery { invoiceRepository.findByInvoiceNumberAndOrderId(invoice1.invoiceNumber, orderId) } returns null
        coEvery { invoiceRepository.findByInvoiceNumberAndOrderId(invoice2.invoiceNumber, orderId) } returns null
        coEvery { invoiceRepository.save(invoice1.copy(orderId = orderId)) } returns savedInvoice1
        coEvery { invoiceRepository.save(invoice2.copy(orderId = orderId)) } returns savedInvoice2

        // Act
        val result = invoiceService.saveInvoicesForOrder(listOf(invoice1, invoice2), orderId)

        // Assert
        assertEquals(2, result.size)
        assertEquals(savedInvoice1.id, result[0].id)
        assertEquals(savedInvoice1.invoiceNumber, result[0].invoiceNumber)
        assertEquals(orderId, result[0].orderId)
        assertEquals(savedInvoice2.id, result[1].id)
        assertEquals(savedInvoice2.invoiceNumber, result[1].invoiceNumber)
        assertEquals(orderId, result[1].orderId)
    }

    @Test
    fun manejaListaVacia(): Unit = runTest {
        // Arrange
        val orderId = 1L

        // Act
        val result = invoiceService.saveInvoicesForOrder(emptyList(), orderId)

        // Assert
        assertEquals(0, result.size)
    }

    @Test
    fun `no guarda facturas duplicadas`(): Unit = runTest {
        // Arrange
        val orderId = 1L
        val invoice = Invoice(
            id = null,
            invoiceNumber = "INV-001",
            orderId = null
        )

        // Simular que la factura ya existe
        coEvery {
            invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId)
        } returns Invoice(id = 10L, invoiceNumber = "INV-001", orderId = orderId)

        // No debería llamarse al método save

        // Act
        val result = invoiceService.saveInvoicesForOrder(listOf(invoice), orderId)

        // Assert
        assertEquals(1, result.size) // No se debería guardar ninguna factura
    }

    @Test
    fun `guarda facturas no duplicadas y omite las duplicadas`(): Unit = runTest {
        // Arrange
        val orderId = 1L
        val invoice1 = Invoice(id = null, invoiceNumber = "INV-001", orderId = null)
        val invoice2 = Invoice(id = null, invoiceNumber = "INV-002", orderId = null)

        val savedInvoice2 = Invoice(id = 2L, invoiceNumber = "INV-002", orderId = orderId)

        // Simular que la primera factura ya existe pero la segunda no
        coEvery {
            invoiceRepository.findByInvoiceNumberAndOrderId("INV-001", orderId)
        } returns Invoice(id = 10L, invoiceNumber = "INV-001", orderId = orderId)

        coEvery {
            invoiceRepository.findByInvoiceNumberAndOrderId("INV-002", orderId)
        } returns null

        coEvery {
            invoiceRepository.save(invoice2.copy(orderId = orderId))
        } returns savedInvoice2

        // Act
        val result = invoiceService.saveInvoicesForOrder(listOf(invoice1, invoice2), orderId)

        // Assert
        assertEquals(2, result.size) // Solo se debería guardar una factura
        assertEquals(savedInvoice2.id, result[1].id)
        assertEquals("INV-002", result[1].invoiceNumber)
    }

    @Test
    fun `throws exception when findByInvoiceNumberAndOrderId fails`() = runTest {
        // Arrange
        val orderId = 1L
        val invoice = Invoice(
            id = null,
            invoiceNumber = "INV-001",
            orderId = null
        )

        coEvery {
            invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId)
        } throws Exception("Database error")

        // Act & Assert
        val exception = assertFailsWith<Exception> {
            invoiceService.saveInvoicesForOrder(listOf(invoice), orderId)
        }

        assertEquals("Database error", exception.message)
    }

    @Test
    fun `throws exception when save invoice fails`() = runTest {
        // Arrange
        val orderId = 1L
        val invoice = Invoice(
            id = null,
            invoiceNumber = "INV-001",
            orderId = null
        )

        coEvery {
            invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId)
        } returns null

        coEvery {
            invoiceRepository.save(invoice.copy(orderId = orderId))
        } throws Exception("Error saving invoice")

        // Act & Assert
        val exception = assertFailsWith<Exception> {
            invoiceService.saveInvoicesForOrder(listOf(invoice), orderId)
        }

        assertEquals("Error saving invoice", exception.message)
    }

    @Test
    fun `throws exception when findAllByOrderId fails`() = runTest {
        // Arrange
        val orderId = 1L

        coEvery {
            invoiceRepository.findAllByOrderId(orderId)
        } throws Exception("Error retrieving invoices")

        // Act & Assert
        val exception = assertFailsWith<Exception> {
            invoiceService.findAllByOrderId(orderId).collect { }
        }

        assertEquals("Error retrieving invoices", exception.message)
    }

    @Test
    fun reutilizaFacturasExistentes(): Unit = runTest {
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

        coEvery { invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId) } returns existingInvoice

        // Act
        val result = invoiceService.saveInvoicesForOrder(listOf(invoice), orderId)

        // Assert
        assertEquals(1, result.size)
        assertEquals(existingInvoice.id, result[0].id)
        assertEquals(existingInvoice.invoiceNumber, result[0].invoiceNumber)
        assertEquals(orderId, result[0].orderId)
    }

    @Test
    fun mezclaFacturasExistentesYNuevas(): Unit = runTest {
        // Arrange
        val orderId = 1L
        val invoice1 = Invoice(id = null, invoiceNumber = "INV-001", orderId = null)
        val invoice2 = Invoice(id = null, invoiceNumber = "INV-002", orderId = null)

        val existingInvoice = Invoice(id = 1L, invoiceNumber = "INV-001", orderId = orderId)
        val savedInvoice = Invoice(id = 2L, invoiceNumber = "INV-002", orderId = orderId)

        coEvery { invoiceRepository.findByInvoiceNumberAndOrderId(invoice1.invoiceNumber, orderId) } returns existingInvoice
        coEvery { invoiceRepository.findByInvoiceNumberAndOrderId(invoice2.invoiceNumber, orderId) } returns null
        coEvery { invoiceRepository.save(invoice2.copy(orderId = orderId)) } returns savedInvoice

        // Act
        val result = invoiceService.saveInvoicesForOrder(listOf(invoice1, invoice2), orderId)

        // Assert
        assertEquals(2, result.size)
        assertEquals(existingInvoice.id, result[0].id)
        assertEquals(savedInvoice.id, result[1].id)
    }

    @Test
    fun manejaExcepcionEnGuardado(): Unit = runTest {
        // Arrange
        val orderId = 1L
        val invoice = Invoice(id = null, invoiceNumber = "INV-001", orderId = null)

        coEvery { invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId) } returns null
        coEvery { invoiceRepository.save(invoice.copy(orderId = orderId)) } throws RuntimeException("Error de base de datos")

        // Act & Assert
        assertFailsWith<RuntimeException>("Error de base de datos") {
            invoiceService.saveInvoicesForOrder(listOf(invoice), orderId)
        }
    }

    @Test
    fun recuperaFacturasPorOrdenId(): Unit = runTest {
        // Arrange
        val orderId = 1L
        val invoices = listOf(
            Invoice(id = 1L, invoiceNumber = "INV-001", orderId = orderId),
            Invoice(id = 2L, invoiceNumber = "INV-002", orderId = orderId)
        )

        every { invoiceRepository.findAllByOrderId(orderId) } returns flowOf(*invoices.toTypedArray())

        // Act
        val result = invoiceService.findAllByOrderId(orderId).toList()

        // Assert
        assertEquals(2, result.size)
        assertEquals("INV-001", result[0].invoiceNumber)
        assertEquals("INV-002", result[1].invoiceNumber)
    }

    @Test
    fun manejaExcepcionEnRecuperacionDeFacturas(): Unit = runTest {
        // Arrange
        val orderId = 1L
        val errorMessage = "Error al leer de base de datos"

        every { invoiceRepository.findAllByOrderId(orderId) } throws RuntimeException(errorMessage)

        // Act & Assert
        val exception = assertFailsWith<RuntimeException> {
            invoiceService.findAllByOrderId(orderId).toList()
        }

        assertEquals(errorMessage, exception.message)
    }
}