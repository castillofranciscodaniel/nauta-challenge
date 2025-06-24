package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.BookingSaveOrchestrationService
import com.challenge.nauta_challenge.infrastructure.delivery.dto.BookingDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.ContainerDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.OrderDto
import com.challenge.nauta_challenge.infrastructure.delivery.dto.InvoiceDto
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
class BookingControllerTest {

    private val bookingSaveOrchestrationService = mockk<BookingSaveOrchestrationService>()
    private val bookingController = BookingController(bookingSaveOrchestrationService)

    @Test
    fun `createBooking should save booking and return created response`() {
        // Given
        val bookingDto = BookingDto(
            booking = "BOOK-123",
            containers = listOf(ContainerDto(container = "CONT-001")),
            orders = listOf(OrderDto(
                purchase = "PO-123",
                invoices = listOf(InvoiceDto(invoice = "INV-001"))
            ))
        )

        val expectedSavedBooking = Booking(
            id = 1L,
            bookingNumber = "BOOK-123",
            userId = 100L,
            containers = listOf(Container(
                id = 1L,
                containerNumber = "CONT-001",
                bookingId = 1L
            )),
            orders = listOf(Order(
                id = 1L,
                purchaseNumber = "PO-123",
                bookingId = 1L,
                invoices = emptyList()
            ))
        )

        every {
            bookingSaveOrchestrationService.saveBooking(any())
        } returns Mono.just(expectedSavedBooking)

        // When & Then
        StepVerifier.create(bookingController.createBooking(bookingDto))
            .assertNext { response ->
                assertEquals(HttpStatus.CREATED, response.statusCode)
                assertNotNull(response.body)
                assertEquals(expectedSavedBooking, response.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createBooking should handle empty containers and orders`() {
        // Given
        val bookingDto = BookingDto(
            booking = "BOOK-456"
        )

        val expectedSavedBooking = Booking(
            id = 2L,
            bookingNumber = "BOOK-456",
            userId = 100L,
            containers = emptyList(),
            orders = emptyList()
        )

        every {
            bookingSaveOrchestrationService.saveBooking(any())
        } returns Mono.just(expectedSavedBooking)

        // When & Then
        StepVerifier.create(bookingController.createBooking(bookingDto))
            .assertNext { response ->
                assertEquals(HttpStatus.CREATED, response.statusCode)
                assertNotNull(response.body)
                assertEquals(expectedSavedBooking, response.body)
            }
            .verifyComplete()
    }
}
