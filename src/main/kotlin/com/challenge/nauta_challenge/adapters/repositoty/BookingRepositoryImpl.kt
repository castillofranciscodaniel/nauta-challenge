package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.BookingDao
import com.challenge.nauta_challenge.infrastructure.repository.model.BookingEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BookingRepositoryImpl(
    private val bookingDao: BookingDao
) : BookingRepository {
    private val logger = LoggerFactory.getLogger(BookingRepositoryImpl::class.java)

    override suspend fun save(booking: Booking): Booking {
        logger.info("[save] Attempting to save booking: bookingNumber=${booking.bookingNumber}, userId=${booking.userId}")

        return runCatching {
            val bookingEntity = BookingEntity.fromModel(booking)
            bookingDao.save(bookingEntity)
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.info("[save] Successfully saved booking: id=${it.id}, bookingNumber=${it.bookingNumber}") }
                ?: throw ModelNotSavedException("Booking not saved")
        }.getOrElse { e ->
            logger.error("[save] Error while saving booking: bookingNumber=${booking.bookingNumber}", e)
            throw ModelNotSavedException("Booking not saved: ${e.message}")
        }
    }

    override suspend fun findByBookingNumberAndUserId(bookingNumber: String, userId: Long): Booking? {
        logger.debug("[findByBookingNumberAndUserId] Looking for booking: bookingNumber=$bookingNumber, userId=$userId")

        return runCatching {
            bookingDao.findByBookingNumberAndUserId(bookingNumber, userId)
                .awaitSingleOrNull()
                ?.toModel()
                ?.also { logger.debug("[findByBookingNumberAndUserId] Found booking: id=${it.id}, bookingNumber=$bookingNumber") }
                ?: run {
                    logger.debug("[findByBookingNumberAndUserId] Booking not found: bookingNumber=$bookingNumber, userId=$userId")
                    null
                }
        }.getOrElse { e ->
            logger.warn("[findByBookingNumberAndUserId] Error looking for booking: bookingNumber=$bookingNumber, userId=$userId", e)
            throw RepositoryException("Error finding booking", e)
        }
    }
}