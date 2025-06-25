package com.challenge.nauta_challenge.infrastructure.repository.dao

import com.challenge.nauta_challenge.infrastructure.repository.model.InvoiceEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface InvoiceDao : R2dbcRepository<InvoiceEntity, Long> {
    fun findAllByOrderId(orderId: Long): Flux<InvoiceEntity>
    fun findByInvoiceNumberAndOrderId(invoiceNumber: String, orderId: Long): Mono<InvoiceEntity>
}
