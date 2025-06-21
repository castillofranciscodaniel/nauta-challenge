package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.objects.AssetUtilsTestObject
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ExtendWith(MockitoExtension::class)

class BookingServiceTest() {

    private val bookingRepository = mockk<BookingRepository>()
    private val containerRepository = mockk<ContainerRepository>()
    private val orderRepository = mockk<OrderRepository>()
    private val userLoggedService = mockk<UserLoggedService>()


    private val bookingService = BookingService(
        bookingRepository,
        containerRepository,
        orderRepository,
        userLoggedService
    )

    @Test
    fun `saveBooking ok when nothing exist`() {
        // When
        val userLoggged = AssetUtilsTestObject.getUser()
        val bookingNumber = "123456"
        val booking = AssetUtilsTestObject.getBookingWithoutId()

        coEvery { userLoggedService.getCurrentUserId() }.returns(userLoggged)

        coEvery { bookingRepository.findByBookingNumberAndUserId(bookingNumber, userLoggged.id!!) }
            .returns(null)

        coEvery { bookingRepository.save(booking) }.returns(booking.copy(id = 1))
        // Then

        val bookingSaved = runBlocking {
            bookingService.saveBooking(booking)
        }

        // Then
        runBlocking {
            assertEquals(1, bookingSaved.id)
        }

    }
}