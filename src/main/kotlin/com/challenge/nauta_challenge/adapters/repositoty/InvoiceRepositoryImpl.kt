package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.InvoiceDao
import com.challenge.nauta_challenge.infrastructure.repository.model.InvoiceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    override suspend fun save(invoice: Invoice): Invoice {
        val invoiceEntity = InvoiceEntity.fromModel(invoice)
        return invoiceDao.save(invoiceEntity).awaitSingleOrNull()?.toModel()
            ?: throw ModelNotSavedException("Invoice not saved")
    }

    override fun findAllByOrderId(orderId: Long): Flow<Invoice> {
        return invoiceDao.findAllByOrderId(orderId)
            .map { it.toModel() }
            .asFlow()
    }

    override suspend fun findByInvoiceNumberAndOrderId(invoiceNumber: String, orderId: Long): Invoice? {
        return invoiceDao.findByInvoiceNumberAndOrderId(invoiceNumber, orderId)
            .awaitSingleOrNull()?.toModel()
    }
}