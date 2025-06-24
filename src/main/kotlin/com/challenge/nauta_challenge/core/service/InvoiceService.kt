package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// InvoiceService.kt
@Service
class InvoiceService(private val invoiceRepository: InvoiceRepository) {
    private val logger = LoggerFactory.getLogger(InvoiceService::class.java)

    fun saveInvoicesForOrder(invoices: List<Invoice>, orderId: Long): Mono<List<Invoice>> {
        logger.info("[saveInvoicesForOrder] Saving ${invoices.size} invoices for orderId=$orderId")

        return Flux.fromIterable(invoices)
            .flatMap { invoice ->
                invoiceRepository.findByInvoiceNumberAndOrderId(invoice.invoiceNumber, orderId)
                    .map { existingInvoice ->
                        logger.debug("[saveInvoicesForOrder] Invoice already exists: id=${existingInvoice.id}, invoiceNumber=${existingInvoice.invoiceNumber}")
                        existingInvoice
                    }
                    .switchIfEmpty(
                        Mono.defer {
                            invoiceRepository.save(invoice.copy(orderId = orderId))
                                .doOnSuccess { saved ->
                                    logger.info("[saveInvoicesForOrder] Saved invoice: id=${saved.id}, invoiceNumber=${saved.invoiceNumber}")
                                }
                        }
                    )
                    .doOnError { error ->
                        logger.error("[saveInvoicesForOrder] Error processing invoice: invoiceNumber=${invoice.invoiceNumber}", error)
                    }
            }
            .collectList()
            .doOnSuccess { savedList -> logger.info("[saveInvoicesForOrder] Successfully saved/found ${savedList.size} invoices for orderId=$orderId") }
    }

    fun findAllByOrderId(orderId: Long): Flux<Invoice> {
        logger.debug("[findAllByOrderId] Finding invoices for orderId=$orderId")

        return invoiceRepository.findAllByOrderId(orderId)
            .doOnSubscribe { logger.debug("[findAllByOrderId] Starting to fetch invoices for orderId=$orderId") }
            .doOnComplete { logger.debug("[findAllByOrderId] Completed fetching invoices for orderId=$orderId") }
            .doOnError { error -> logger.error("[findAllByOrderId] Error fetching invoices for orderId=$orderId", error) }
    }
}