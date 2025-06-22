package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.BookingRepositoryImpl
import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.infrastructure.repository.dao.BookingDao
import com.challenge.nauta_challenge.infrastructure.repository.model.BookingEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@SpringBootTest
class BookingRepositoryImplTest {

    private val bookingDao = mockk<BookingDao>()
    private val bookingRepository: BookingRepository = BookingRepositoryImpl(bookingDao)

    @Test
    fun savesBookingSuccessfully() = runBlocking {
        val booking = Booking(id = null, bookingNumber = "B123", userId = 1, containers = emptyList(), orders = emptyList())
        val bookingEntity = BookingEntity.fromModel(booking)

        every { bookingDao.save(bookingEntity) }.returns(Mono.just(bookingEntity.copy(id = 1)))

        val result = bookingRepository.save(booking)

        assertEquals(1, result.id)
        assertEquals("B123", result.bookingNumber)
        assertEquals(1, result.userId)
    }

    @Test
    fun throwsExceptionWhenBookingNotSaved(): Unit = runBlocking {
        val booking = Booking(id = null, bookingNumber = "B123", userId = 1, containers = emptyList(), orders = emptyList())
        val bookingEntity = BookingEntity.fromModel(booking)

        every { bookingDao.save(bookingEntity) }.returns(Mono.empty())

        assertFailsWith<Exception>("Booking not saved") {
            bookingRepository.save(booking)
        }
    }

    @Test
    fun findsBookingByBookingNumberAndUserId() = runBlocking {
        val bookingEntity = BookingEntity(id = 1, bookingNumber = "B123", userId = 1)
        
        every { bookingDao.findByBookingNumberAndUserId("B123", 1) }
            .returns(Mono.just(bookingEntity))

        val result = bookingRepository.findByBookingNumberAndUserId("B123", 1)

        assertEquals(1, result?.id)
        assertEquals("B123", result?.bookingNumber)
        assertEquals(1, result?.userId)
    }

    @Test
    fun returnsNullWhenBookingNotFound() = runBlocking {
        every { bookingDao.findByBookingNumberAndUserId("B123", 1) }.returns(Mono.empty())

        val result = bookingRepository.findByBookingNumberAndUserId("B123", 1)

        assertNull(result)
    }

    @Test
    fun `findAllByUserId returns flow of bookings`() = runBlocking {
        // Arrange
        val userId = 1L
        val bookingEntity1 = BookingEntity(id = 1, bookingNumber = "B123", userId = userId)
        val bookingEntity2 = BookingEntity(id = 2, bookingNumber = "B456", userId = userId)

        every { bookingDao.findAllByUserId(userId) } returns
            Flux.just(bookingEntity1, bookingEntity2)

        // Act
        val result = bookingRepository.findAllByUserId(userId)
        val bookings = result.toList()

        // Assert
        assertEquals(2, bookings.size)
        assertEquals(1L, bookings[0].id)
        assertEquals("B123", bookings[0].bookingNumber)
        assertEquals(userId, bookings[0].userId)
        assertEquals(2L, bookings[1].id)
        assertEquals("B456", bookings[1].bookingNumber)
        assertEquals(userId, bookings[1].userId)
    }

    @Test
    fun `findAllByUserId returns empty flow when no bookings found`() = runBlocking {
        // Arrange
        val userId = 1L

        every { bookingDao.findAllByUserId(userId) } returns
            Flux.empty()

        // Act
        val result = bookingRepository.findAllByUserId(userId)
        val bookings = result.toList()

        // Assert
        assertEquals(0, bookings.size)
    }
}