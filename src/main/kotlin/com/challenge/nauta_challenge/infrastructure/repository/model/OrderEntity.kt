package com.challenge.nauta_challenge.infrastructure.repository.model

import com.challenge.nauta_challenge.core.model.Order
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("orders")
data class OrderEntity(
    @Id
    val id: Long? = null,
    @Column("purchase_number") val purchaseNumber: String,
    @Column("booking_id") val bookingId: Long?
) {
    companion object {
        fun fromModel(model: Order) = OrderEntity(
            id = model.id,
            purchaseNumber = model.purchaseNumber,
            bookingId = model.bookingId
        )
    }

    fun toModel() = Order(
        id = this.id,
        purchaseNumber = this.purchaseNumber,
        bookingId = this.bookingId
    )
}