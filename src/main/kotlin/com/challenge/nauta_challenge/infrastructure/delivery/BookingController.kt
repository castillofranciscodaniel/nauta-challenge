package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.service.BookingService
import com.challenge.nauta_challenge.infrastructure.delivery.dto.BookingDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bookings")
class BookingController(private val bookingService: BookingService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createBooking(@RequestBody booking: BookingDto): ResponseEntity<Booking> {
        val savedBooking = bookingService.saveBooking(booking.toModel())
        return ResponseEntity(savedBooking, HttpStatus.CREATED)
    }
}