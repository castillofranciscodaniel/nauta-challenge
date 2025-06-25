package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// BookingService.kt
@Service
class BookingService(private val bookingRepository: BookingRepository) {
    private val logger = LoggerFactory.getLogger(BookingService::class.java)

    suspend fun findOrSaveBooking(booking: Booking, userId: Long): Booking {
        logger.info("[findOrSaveBooking] Procesando booking: {}, userID: {}", booking.bookingNumber, userId)

        return try {
            bookingRepository.findByBookingNumberAndUserId(booking.bookingNumber, userId)?.also {
                logger.debug("[findOrSaveBooking] Booking encontrado con ID: {}", it.id)
            } ?: bookingRepository.save(booking.copy(userId = userId)).also {
                logger.info("[findOrSaveBooking] Booking guardado con ID: {}", it.id)
            }
        } catch (e: Exception) {
            logger.error("[findOrSaveBooking] Error procesando booking: {}", booking.bookingNumber, e)
            throw e
        }
    }
}
