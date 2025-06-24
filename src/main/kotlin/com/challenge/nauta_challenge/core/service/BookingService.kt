package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class BookingService(private val bookingRepository: BookingRepository) {
    private val logger = LoggerFactory.getLogger(BookingService::class.java)

    fun findOrSaveBooking(
        booking: Booking,
        userId: Long
    ): Mono<Booking> {
        logger.info("[findOrSaveBooking] Looking for booking: bookingNumber=${booking.bookingNumber}, userId=$userId")

        return bookingRepository.findByBookingNumberAndUserId(booking.bookingNumber, userId)
            .switchIfEmpty(
                bookingRepository.save(booking.copy(userId = userId))
                    .doOnSubscribe { logger.info("[findOrSaveBooking] Booking not found, creating new: bookingNumber=${booking.bookingNumber}") }
            )
            .doOnSuccess { foundOrSavedBooking ->
                if (foundOrSavedBooking.id != null) {
                    logger.info("[findOrSaveBooking] ${if (foundOrSavedBooking.id == booking.id) "Found" else "Saved"} booking: id=${foundOrSavedBooking.id}, bookingNumber=${foundOrSavedBooking.bookingNumber}")
                }
            }
            .onErrorMap { e ->
                logger.error("[findOrSaveBooking] Error processing booking: bookingNumber=${booking.bookingNumber}", e)
                RuntimeException("Error processing booking: ${e.message}")
            }
    }
}
