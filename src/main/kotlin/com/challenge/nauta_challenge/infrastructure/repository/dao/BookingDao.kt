package com.challenge.nauta_challenge.infrastructure.repository.dao

import com.challenge.nauta_challenge.infrastructure.repository.model.BookingEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface BookingDao : R2dbcRepository<BookingEntity, Long> {
    fun findByBookingNumberAndUserId(bookingNumber: String, userId: Long): Mono<BookingEntity>
    fun findAllByUserId(userId: Long): Flux<BookingEntity>
}

