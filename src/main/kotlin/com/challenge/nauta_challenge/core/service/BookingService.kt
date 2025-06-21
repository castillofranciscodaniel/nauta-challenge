package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
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
        val userLogged = userLoggedService.getCurrentUserId()

        val bookingSaved = bookingRepository.findByBookingNumberAndUserId(
            booking.bookingNumber, userLogged.id!!
        ) ?: bookingRepository.save(booking)


        return bookingSaved
    }
}