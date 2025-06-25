package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.REQUIRED)
class BookingSaveOrchestrationService(
    private val bookingService: BookingService,
    private val userLoggedService: UserLoggedService,
    private val containerService: ContainerService,
    private val orderService: OrderService,
    private val associationService: OrderContainerAssociationService
) {
    private val logger = LoggerFactory.getLogger(BookingSaveOrchestrationService::class.java)

    suspend fun saveBooking(booking: Booking): Booking {
        logger.info("[saveBooking] Iniciando proceso de guardado de booking: {}", booking.bookingNumber)

        return try {
            val userId = userLoggedService.getCurrentUser().id

            val bookingSaved = bookingService.findOrSaveBooking(
                booking = booking,
                userId = userId!!
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

            bookingSaved.copy(containers = containersSaved, orders = ordersSaved)
        } catch (e: Exception) {
            logger.error("[saveBooking] Error while saving booking: {}", booking.bookingNumber, e)
            throw e
        }
    }
}