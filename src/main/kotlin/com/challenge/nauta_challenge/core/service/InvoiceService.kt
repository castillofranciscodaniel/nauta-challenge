package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// InvoiceService.kt
@Service
class InvoiceService(private val invoiceRepository: InvoiceRepository) {
    private val logger = LoggerFactory.getLogger(InvoiceService::class.java)

    suspend fun saveInvoicesForOrder(invoices: List<Invoice>, orderId: Long): List<Invoice> {
        logger.info("[saveInvoicesForOrder] Guardando {} facturas para la orden ID: {}", invoices.size, orderId)

        return try {
            val result = invoices.map { invoice ->
                invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId)
                    ?.also {
                        logger.debug("[saveInvoicesForOrder] Factura encontrada existente con ID: {}", it.id)
                    }
                    ?: invoiceRepository.save(invoice.copy(orderId = orderId)).also {
                        logger.debug("[saveInvoicesForOrder] Nueva factura guardada con ID: {}", it.id)
                    }
            }

            logger.info("[saveInvoicesForOrder] Guardadas {} facturas exitosamente", result.size)
            result
        } catch (e: Exception) {
            logger.error("[saveInvoicesForOrder] Error general al guardar facturas para la orden ID: {}", orderId, e)
            throw e
        }
    }

    fun findAllByOrderId(orderId: Long): Flow<Invoice> {
        logger.info("[findAllByOrderId] Buscando facturas para la orden ID: {}", orderId)

        return invoiceRepository.findAllByOrderId(orderId)
            .catch { e ->
                logger.error("[findAllByOrderId] Error al recuperar facturas para orden ID: {}", orderId, e)
                throw e
            }
    }

}