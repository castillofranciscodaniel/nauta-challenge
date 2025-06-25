package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.InvoiceDao
import com.challenge.nauta_challenge.infrastructure.repository.model.InvoiceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    private val logger = LoggerFactory.getLogger(InvoiceRepositoryImpl::class.java)

    override suspend fun save(invoice: Invoice): Invoice {
        logger.info("[save] Attempting to save invoice: invoiceNumber=${invoice.invoiceNumber}, orderId=${invoice.orderId}")

        return try {
            val invoiceEntity = InvoiceEntity.fromModel(invoice)
            invoiceDao.save(invoiceEntity)
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.info("[save] Successfully saved invoice: id=${it.id}, invoiceNumber=${it.invoiceNumber}") }
                ?: throw ModelNotSavedException("Invoice not saved")
        } catch (e: Exception) {
            logger.error("[save] Error while saving invoice: invoiceNumber=${invoice.invoiceNumber}", e)
            throw ModelNotSavedException("Invoice not saved: ${e.message}")
        }
    }

    override fun findAllByOrderId(orderId: Long): Flow<Invoice> {
        logger.debug("[findAllByOrderId] Looking for invoices for orderId=$orderId")

        return try {
            invoiceDao.findAllByOrderId(orderId)
                .map { it.toModel() }
                .asFlow()
                .onStart { logger.debug("[findAllByOrderId] Starting invoice search for orderId=$orderId") }
                .onCompletion { error ->
                    if (error == null) {
                        logger.debug("[findAllByOrderId] Completed invoice search for orderId=$orderId")
                    }
                }
        } catch (e: Exception) {
            logger.error("[findAllByOrderId] Error retrieving invoices for orderId=$orderId", e)
            flow { throw RepositoryException("Error retrieving invoices for order", e) }
        }
    }

    override suspend fun findByInvoiceNumberAndOrderId(invoiceNumber: String, orderId: Long): Invoice? {
        logger.debug("[findByInvoiceNumberAndOrderId] Looking for invoice: invoiceNumber=$invoiceNumber, orderId=$orderId")

        return try {
            invoiceDao.findByInvoiceNumberAndOrderId(invoiceNumber, orderId)
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.debug("[findByInvoiceNumberAndOrderId] Found invoice: id=${it.id}") }
                ?: run {
                    logger.debug("[findByInvoiceNumberAndOrderId] Invoice not found: invoiceNumber=$invoiceNumber, orderId=$orderId")
                    null
                }
        } catch (e: Exception) {
            logger.warn(
                "[findByInvoiceNumberAndOrderId] Error looking for invoice: invoiceNumber=$invoiceNumber, orderId=$orderId",
                e
            )
            throw RepositoryException("Error finding invoice", e)
        }
    }
}