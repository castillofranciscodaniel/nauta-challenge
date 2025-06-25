package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.service.BookingSaveOrchestrationService
import com.challenge.nauta_challenge.infrastructure.delivery.dto.BookingDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bookings")
class BookingController(private val bookingSaveOrchestrationService: BookingSaveOrchestrationService) {
    private val logger = LoggerFactory.getLogger(BookingController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createBooking(@RequestBody booking: BookingDto): ResponseEntity<Booking> {
        logger.info("[createBooking] Iniciando creación de booking: {}", booking.booking)

        return try {
            val savedBooking = bookingSaveOrchestrationService.saveBooking(booking.toModel())
            logger.info("[createBooking] Booking creado exitosamente con ID: {}", savedBooking.id)
            ResponseEntity(savedBooking, HttpStatus.CREATED)
        } catch (e: Exception) {
            logger.error("[createBooking] Error al procesar booking: {}", booking.booking, e)
            throw e
        }
    }
}