package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.BookingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertFailsWith

@SpringBootTest
class BookingServiceTest {

    private val bookingRepository = mockk<BookingRepository>()
    private val bookingService = BookingService(bookingRepository)

    @Test
    fun devuelveBookingExistenteSiYaEstáAlmacenado(): Unit = runTest {
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
        
        coEvery { bookingRepository.findByBookingNumberAndUserId(bookingNumber, userId) } returns existingBooking
        
        // Act
        val result = bookingService.findOrSaveBooking(booking, userId)
        
        // Assert
        assertEquals(existingBooking.id, result.id)
        assertEquals(bookingNumber, result.bookingNumber)
        assertEquals(userId, result.userId)
        assertSame(existingBooking, result)
    }
    
    @Test
    fun guardarBookingCuandoNoExiste(): Unit = runTest {
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
        
        coEvery { bookingRepository.findByBookingNumberAndUserId(bookingNumber, userId) } returns null
        coEvery { bookingRepository.save(booking.copy(userId = userId)) } returns savedBooking
        
        // Act
        val result = bookingService.findOrSaveBooking(booking, userId)
        
        // Assert
        assertEquals(savedBooking.id, result.id)
        assertEquals(bookingNumber, result.bookingNumber)
        assertEquals(userId, result.userId)
        assertSame(savedBooking, result)
    }

    @Test
    fun `propaga excepción cuando findByBookingNumberAndUserId falla`(): Unit = runTest {
        // Arrange
        val userId = 1L
        val bookingNumber = "B12345"
        val booking = Booking(
            id = null,
            bookingNumber = bookingNumber,
            userId = null
        )

        coEvery {
            bookingRepository.findByBookingNumberAndUserId(bookingNumber, userId)
        } throws Exception("Error al buscar booking")

        // Act & Assert
        val exception = assertFailsWith<Exception> {
            bookingService.findOrSaveBooking(booking, userId)
        }

        assertEquals("Error al buscar booking", exception.message)
    }

    @Test
    fun `propaga excepción cuando save falla`(): Unit = runTest {
        // Arrange
        val userId = 1L
        val bookingNumber = "B12345"
        val booking = Booking(
            id = null,
            bookingNumber = bookingNumber,
            userId = null
        )

        coEvery {
            bookingRepository.findByBookingNumberAndUserId(bookingNumber, userId)
        } returns null

        coEvery {
            bookingRepository.save(booking.copy(userId = userId))
        } throws Exception("Error al guardar booking")

        // Act & Assert
        val exception = assertFailsWith<Exception> {
            bookingService.findOrSaveBooking(booking, userId)
        }

        assertEquals("Error al guardar booking", exception.message)
    }
}