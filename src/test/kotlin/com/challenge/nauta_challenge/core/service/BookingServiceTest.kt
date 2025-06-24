package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals

@SpringBootTest
class BookingServiceTest {

    private val bookingRepository = mockk<BookingRepository>()
    private val bookingService = BookingService(bookingRepository)

    @Test
    fun returnsExistingBookingIfAlreadyStored() {
        // Arrange
        val userId = 1L
        val bookingNumber = "B12345"
        val booking = Booking(
            id = null, 
            bookingNumber = bookingNumber,
            userId = null
        )
        val existingBooking = Booking(
            id = 1L, 
            bookingNumber = bookingNumber, 
            userId = userId
        )
        
        every { bookingRepository.findByBookingNumberAndUserId(bookingNumber, userId) } returns Mono.just(existingBooking)
        // Al encontrar una reserva existente, no debería intentar guardarla

        // Act & Assert
        StepVerifier.create(bookingService.findOrSaveBooking(booking, userId))
            .assertNext { result ->
                assertEquals(existingBooking.id, result.id)
                assertEquals(existingBooking.bookingNumber, result.bookingNumber)
                assertEquals(existingBooking.userId, result.userId)
            }
            .verifyComplete()
    }
    
    @Test
    fun savesNewBookingWhenNotFound() {
        // Arrange
        val userId = 1L
        val bookingNumber = "B12345"
        val booking = Booking(
            id = null, 
            bookingNumber = bookingNumber,
            userId = null
        )
        val savedBooking = Booking(
            id = 1L, 
            bookingNumber = bookingNumber, 
            userId = userId
        )
        
        every { bookingRepository.findByBookingNumberAndUserId(bookingNumber, userId) } returns Mono.empty()
        every { bookingRepository.save(booking.copy(userId = userId)) } returns Mono.just(savedBooking)

        // Act & Assert
        StepVerifier.create(bookingService.findOrSaveBooking(booking, userId))
            .assertNext { result ->
                assertEquals(savedBooking.id, result.id)
                assertEquals(savedBooking.bookingNumber, result.bookingNumber)
                assertEquals(savedBooking.userId, result.userId)
            }
            .verifyComplete()
    }
}