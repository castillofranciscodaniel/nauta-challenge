package com.challenge.nauta_challenge.core.repository

import com.challenge.nauta_challenge.adapters.repositoty.BookingRepositoryImpl
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.infrastructure.repository.dao.BookingDao
import com.challenge.nauta_challenge.infrastructure.repository.model.BookingEntity
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.*

@SpringBootTest
class BookingRepositoryImplTest {

    private val bookingDao = mockk<BookingDao>()
    private val bookingRepository: BookingRepository = BookingRepositoryImpl(bookingDao)

    @Test
    fun savesBookingSuccessfully() {
        val booking = Booking(id = null, bookingNumber = "BK123", userId = 1)
        val bookingEntity = BookingEntity(id = null, bookingNumber = "BK123", userId = 1)

        every { bookingDao.save(bookingEntity) }.returns(Mono.just(bookingEntity.copy(id = 1)))

        StepVerifier.create(bookingRepository.save(booking))
            .assertNext { result ->
                assertEquals(1, result.id)
                assertEquals("BK123", result.bookingNumber)
                assertEquals(1, result.userId)
            }
            .verifyComplete()
    }

    @Test
    fun throwsExceptionWhenBookingNotSaved() {
        val booking = Booking(id = null, bookingNumber = "BK123", userId = 1)
        val bookingEntity = BookingEntity(id = null, bookingNumber = "BK123", userId = 1)

        // Mock para devolver Mono.empty()
        every { bookingDao.save(bookingEntity) }.returns(Mono.empty())

        // Verificar la excepción sin validar el mensaje exacto
        StepVerifier.create(bookingRepository.save(booking))
            .expectError(ModelNotSavedException::class.java)
            .verify()
    }

    @Test
    fun findsBookingByBookingNumberAndUserId() {
        val bookingEntity = BookingEntity(id = 1, bookingNumber = "BK123", userId = 1)

        every { bookingDao.findByBookingNumberAndUserId("BK123", 1) }
            .returns(Mono.just(bookingEntity))

        StepVerifier.create(bookingRepository.findByBookingNumberAndUserId("BK123", 1))
            .assertNext { result ->
                assertEquals(1, result.id)
                assertEquals("BK123", result.bookingNumber)
                assertEquals(1, result.userId)
            }
            .verifyComplete()
    }

    @Test
    fun returnsEmptyMonoWhenBookingNotFound() {
        every { bookingDao.findByBookingNumberAndUserId("BK123", 1) }.returns(Mono.empty())

        StepVerifier.create(bookingRepository.findByBookingNumberAndUserId("BK123", 1))
            .verifyComplete()
    }

    @Test
    fun throwsRepositoryExceptionWhenErrorDuringFindByBookingNumberAndUserId() {
        val bookingNumber = "BK123"
        val userId = 1L

        // Simular una excepción durante la llamada al DAO
        every { bookingDao.findByBookingNumberAndUserId(bookingNumber, userId) }.returns(
            Mono.error(RuntimeException("Database connection error"))
        )

        StepVerifier.create(bookingRepository.findByBookingNumberAndUserId(bookingNumber, userId))
            .expectError(RepositoryException::class.java)
            .verify()
    }

}