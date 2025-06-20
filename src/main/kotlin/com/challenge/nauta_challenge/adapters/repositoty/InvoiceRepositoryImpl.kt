package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.NotFoundException
import com.challenge.nauta_challenge.core.model.Invoice
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.InvoiceDao
import com.challenge.nauta_challenge.infrastructure.repository.model.InvoiceEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    override suspend fun save(invoice: Invoice): Invoice {
        val invoiceEntity = InvoiceEntity.fromModel(invoice)
        return invoiceDao.save(invoiceEntity).awaitSingleOrNull()?.toModel()
            ?: throw Exception("Invoice not saved")
    }
}