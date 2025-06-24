package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.InvoiceDao
import com.challenge.nauta_challenge.infrastructure.repository.model.InvoiceEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    private val logger = LoggerFactory.getLogger(InvoiceRepositoryImpl::class.java)

    override fun save(invoice: Invoice): Mono<Invoice> {
        logger.info("[save] Attempting to save invoice: invoiceNumber=${invoice.invoiceNumber}, orderId=${invoice.orderId}")

        val invoiceEntity = InvoiceEntity.fromModel(invoice)
        return invoiceDao.save(invoiceEntity)
            .map { it.toModel() }
            .doOnSuccess { logger.info("[save] Successfully saved invoice: id=${it.id}, invoiceNumber=${it.invoiceNumber}") }
            .switchIfEmpty(Mono.error(ModelNotSavedException("Invoice not saved")))
            .onErrorMap { e ->
                logger.error("[save] Error while saving invoice: invoiceNumber=${invoice.invoiceNumber}", e)
                ModelNotSavedException("Invoice not saved: ${e.message}")
            }
    }

    override fun findAllByOrderId(orderId: Long): Flux<Invoice> {
        logger.debug("[findAllByOrderId] Looking for invoices for orderId=$orderId")

        return invoiceDao.findAllByOrderId(orderId)
            .map { it.toModel() }
            .doOnSubscribe { logger.debug("[findAllByOrderId] Starting invoice search for orderId=$orderId") }
            .doOnComplete { logger.debug("[findAllByOrderId] Completed invoice search for orderId=$orderId") }
            .onErrorMap { e ->
                logger.error("[findAllByOrderId] Error retrieving invoices for orderId=$orderId", e)
                RepositoryException("Error retrieving invoices for order", e)
            }
    }

    override fun findByInvoiceNumberAndOrderId(invoiceNumber: String, orderId: Long): Mono<Invoice> {
        logger.debug("[findByInvoiceNumberAndOrderId] Looking for invoice: invoiceNumber=$invoiceNumber, orderId=$orderId")

        return invoiceDao.findByInvoiceNumberAndOrderId(invoiceNumber, orderId)
            .map { it.toModel() }
            .doOnSuccess { invoice ->
                if (invoice != null) {
                    logger.debug("[findByInvoiceNumberAndOrderId] Found invoice: id=${invoice.id}")
                } else {
                    logger.debug("[findByInvoiceNumberAndOrderId] Invoice not found: invoiceNumber=$invoiceNumber, orderId=$orderId")
                }
            }
            .onErrorMap { e ->
                logger.warn("[findByInvoiceNumberAndOrderId] Error looking for invoice: invoiceNumber=$invoiceNumber, orderId=$orderId", e)
                RepositoryException("Error finding invoice", e)
            }
    }
}