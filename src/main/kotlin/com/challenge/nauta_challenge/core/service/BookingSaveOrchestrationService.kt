package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

// BookingService.kt
@Service
@Transactional(propagation = Propagation.REQUIRED)
class BookingSaveOrchestrationService(
    private val bookingService: BookingService,
    private val userLoggedService: UserLoggedService,
    private val containerService: ContainerService,
    private val orderService: OrderService,
    private val associationService: OrderContainerAssociationService
) {
    suspend fun saveBooking(booking: Booking): Booking {
        val userId = userLoggedService.getCurrentUserId().id!!

        val bookingSaved = bookingService.findOrSaveBooking(
            booking = booking,
            userId = userId
        )

        val containersSaved = containerService.saveContainersForBooking(
            containers = booking.containers,
            bookingId = bookingSaved.id!!
        )
        val ordersSaved = orderService.saveOrdersForBooking(
            orders = booking.orders,
            bookingId = bookingSaved.id
        )

        associationService.createAssociations(
            orders = ordersSaved,
            containers = containersSaved,
            bookingNumber = booking.bookingNumber
        )

        return bookingSaved.copy(containers = containersSaved, orders = ordersSaved)
    }

}
