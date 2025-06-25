package com.challenge.nauta_challenge.infrastructure.repository.model

import com.challenge.nauta_challenge.core.model.Invoice
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("invoices")
data class InvoiceEntity(
    @Id
    val id: Long? = null,
    @Column("invoice_number") val invoiceNumber: String,
    @Column("order_id") val orderId: Long?
) {
    companion object {
        fun fromModel(model: Invoice) = InvoiceEntity(
            id = model.id,
            invoiceNumber = model.invoiceNumber,
            orderId = model.orderId
        )
    }

    fun toModel() = Invoice(
        id = this.id,
        invoiceNumber = this.invoiceNumber,
        orderId = this.orderId
    )
}