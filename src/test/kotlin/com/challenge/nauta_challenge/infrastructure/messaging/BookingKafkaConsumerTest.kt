package com.challenge.nauta_challenge.infrastructure.messaging

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.service.BookingSaveOrchestrationService
import com.challenge.nauta_challenge.infrastructure.consumer.BookingKafkaConsumer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class BookingKafkaConsumerTest {
    private val bookingSaveOrchestrationService = mockk<BookingSaveOrchestrationService>(relaxed = true)
    private val consumer = BookingKafkaConsumer(
        bookingSaveOrchestrationService,
        failedBookingsTopic = "failed-bookings",
        failedBookingsGroup = "failed-bookings-group"
    )

    @Test
    fun `consumeFailedBooking reprocesa booking correctamente`() = runBlocking {
        val booking = Booking(bookingNumber = "B1", userId = 1, containers = emptyList(), orders = emptyList())
        coEvery { bookingSaveOrchestrationService.retryBookingSave(booking) } returns booking

        consumer.consumeFailedBooking(booking)

        coVerify { bookingSaveOrchestrationService.retryBookingSave(booking) }
    }

    @Test
    fun `consumeFailedBooking maneja excepcion correctamente`() = runTest {
        val booking = Booking(bookingNumber = "B1", userId = 1, containers = emptyList(), orders = emptyList())
        coEvery { bookingSaveOrchestrationService.retryBookingSave(booking) } throws Exception("Simulated exception")

        consumer.consumeFailedBooking(booking)

        // No se lanza excepción, lo que indica que el catch manejó el error correctamente
        coVerify(exactly = 1) { bookingSaveOrchestrationService.retryBookingSave(booking) }
    }
}
