package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

// InvoiceService.kt
@Service
class InvoiceService(private val invoiceRepository: InvoiceRepository) {

    suspend fun saveInvoicesForOrder(invoices: List<Invoice>, orderId: Long): List<Invoice> {
        return invoices.mapNotNull { invoice ->
            invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId)
                ?: invoiceRepository.save(invoice.copy(orderId = orderId))
        }
    }

    fun findAllByOrderId(orderId: Long): Flow<Invoice> {
        return invoiceRepository.findAllByOrderId(orderId)
    }
}