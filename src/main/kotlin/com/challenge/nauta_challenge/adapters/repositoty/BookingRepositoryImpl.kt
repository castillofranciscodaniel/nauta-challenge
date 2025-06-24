package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.BookingDao
import com.challenge.nauta_challenge.infrastructure.repository.model.BookingEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class BookingRepositoryImpl(
    private val bookingDao: BookingDao
) : BookingRepository {
    private val logger = LoggerFactory.getLogger(BookingRepositoryImpl::class.java)

    override fun save(booking: Booking): Mono<Booking> {
        logger.info("[save] Attempting to save booking: bookingNumber=${booking.bookingNumber}, userId=${booking.userId}")

        val bookingEntity = BookingEntity.fromModel(booking)
        return bookingDao.save(bookingEntity)
            .map { it.toModel() }
            .doOnSuccess { logger.info("[save] Successfully saved booking: id=${it.id}, bookingNumber=${it.bookingNumber}") }
            .switchIfEmpty(Mono.error(ModelNotSavedException("Booking not saved")))
            .onErrorMap { e ->
                logger.error("[save] Error while saving booking: bookingNumber=${booking.bookingNumber}", e)
                ModelNotSavedException("Booking not saved: ${e.message}")
            }
    }

    override fun findByBookingNumberAndUserId(bookingNumber: String, userId: Long): Mono<Booking> {
        logger.debug("[findByBookingNumberAndUserId] Looking for booking: bookingNumber=$bookingNumber, userId=$userId")

        return bookingDao.findByBookingNumberAndUserId(bookingNumber, userId)
            .map { it.toModel() }
            .doOnSuccess { booking ->
                if (booking != null) {
                    logger.debug("[findByBookingNumberAndUserId] Found booking: id=${booking.id}, bookingNumber=$bookingNumber")
                } else {
                    logger.debug("[findByBookingNumberAndUserId] Booking not found: bookingNumber=$bookingNumber, userId=$userId")
                }
            }
            .onErrorMap { e ->
                logger.warn("[findByBookingNumberAndUserId] Error looking for booking: bookingNumber=$bookingNumber, userId=$userId", e)
                RepositoryException("Error finding booking", e)
            }
    }
}