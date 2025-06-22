package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import org.springframework.stereotype.Service

// ContainerService.kt
@Service
class BookingService(private val bookingRepository: BookingRepository) {

    suspend fun findOrSaveBooking(
        booking: Booking,
        userId: Long
    ): Booking = (bookingRepository.findByBookingNumberAndUserId(booking.bookingNumber, userId)
        ?: bookingRepository.save(booking.copy(userId = userId)))

}
