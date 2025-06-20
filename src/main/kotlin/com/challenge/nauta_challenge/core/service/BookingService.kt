package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.NotFoundException
import com.challenge.nauta_challenge.core.exception.UnauthorizedException
import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import com.challenge.nauta_challenge.core.repository.UserRepository
import org.apache.commons.logging.Log
import org.springframework.stereotype.Service

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val containerRepository: ContainerRepository,
    private val orderRepository: OrderRepository,
    private val userLoggedService: UserLoggedService
) {

    suspend fun saveBooking(booking: Booking): Booking {
        // Obtener userId del usuario autenticado
        val userId = userLoggedService.getCurrentUserId()

       val bookingSaved = bookingRepository.findByBookingNumberAndUserId(
            booking.bookingNumber, userId
        )

        return bookingSaved
    }
}