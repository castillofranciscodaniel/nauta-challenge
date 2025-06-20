package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.exception.NotFoundException
import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.BookingDao
import com.challenge.nauta_challenge.infrastructure.repository.model.BookingEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class BookingRepositoryImpl(
    private val bookingDao: BookingDao
) : BookingRepository {
    override suspend fun save(booking: Booking): Booking {
        val bookingEntity = BookingEntity.fromModel(booking)
        return bookingDao.save(bookingEntity).awaitSingleOrNull()?.toModel()
            ?: throw Exception("Booking not saved")
    }

    override suspend fun findByBookingNumberAndUserId(bookingNumber: String, userId: Long): Booking =
        bookingDao.findByBookingNumberAndUserId(bookingNumber, userId)
            .awaitSingleOrNull()?.toModel() ?: throw NotFoundException("Booking not found")
}