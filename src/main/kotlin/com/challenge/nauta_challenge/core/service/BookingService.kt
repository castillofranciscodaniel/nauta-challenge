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
            .flatMap { existingBooking ->
                // Si encontramos un booking existente, simplemente lo devolvemos
                logger.info("[findOrSaveBooking] Found existing booking: id=${existingBooking.id}, bookingNumber=${existingBooking.bookingNumber}")
                Mono.just(existingBooking)
            }
            .switchIfEmpty(
                // Solo si no encontramos un booking, creamos uno nuevo
                Mono.defer {
                    logger.info("[findOrSaveBooking] Booking not found, creating new: bookingNumber=${booking.bookingNumber}")
                    bookingRepository.save(booking.copy(userId = userId))
                        .doOnSuccess { savedBooking ->
                            logger.info("[findOrSaveBooking] Saved new booking: id=${savedBooking.id}, bookingNumber=${savedBooking.bookingNumber}")
                        }
                }
            )
            .onErrorMap { e ->
                logger.error("[findOrSaveBooking] Error processing booking: bookingNumber=${booking.bookingNumber}", e)
                RuntimeException("Error processing booking: ${e.message}")
            }
    }
}
