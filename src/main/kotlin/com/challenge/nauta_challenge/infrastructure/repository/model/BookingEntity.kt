package com.challenge.nauta_challenge.infrastructure.repository.model

import com.challenge.nauta_challenge.core.model.Booking
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("bookings")
data class BookingEntity(
    @Id
    val id: Long? = null,
    @Column("booking_number") val bookingNumber: String,
    @Column("user_id") val userId: Long
) {
    companion object {
        fun fromModel(model: Booking) = BookingEntity(
            id = model.id,
            bookingNumber = model.bookingNumber,
            userId = model.userId!!
        )
    }

    fun toModel() = Booking(
        id = this.id,
        bookingNumber = this.bookingNumber,
        userId = this.userId
    )
}



