package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.service.BookingSaveOrchestrationService
import com.challenge.nauta_challenge.infrastructure.delivery.dto.BookingDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/bookings")
class BookingController(private val bookingSaveOrchestrationService: BookingSaveOrchestrationService) {
    private val logger = LoggerFactory.getLogger(BookingController::class.java)

    @PostMapping
    fun createBooking(@RequestBody booking: BookingDto): Mono<ResponseEntity<Booking>> {
        logger.info("[createBooking] Received request to create booking")

        return bookingSaveOrchestrationService.saveBooking(booking.toModel())
            .map { savedBooking ->
                logger.info("[createBooking] Booking created successfully with id=${savedBooking.id}")
                ResponseEntity(savedBooking, HttpStatus.CREATED)
            }
            .doOnError { error ->
                logger.error("[createBooking] Failed to create booking", error)
            }
    }
}