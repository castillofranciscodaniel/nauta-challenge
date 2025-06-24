package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Booking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

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
    private val logger = LoggerFactory.getLogger(BookingSaveOrchestrationService::class.java)

    fun saveBooking(booking: Booking): Mono<Booking> {
        logger.info("[saveBooking] Starting orchestration to save booking: bookingNumber=${booking.bookingNumber}")

        return userLoggedService.getCurrentUserId()
            .flatMap { user ->
                logger.debug("[saveBooking] Got current user, id=${user.id}")

                bookingService.findOrSaveBooking(
                    booking = booking,
                    userId = user.id!!
                )
                .doOnSuccess { saved ->
                    logger.info("[saveBooking] Booking ${if (saved.id == booking.id) "found" else "saved"}: id=${saved.id}, bookingNumber=${saved.bookingNumber}")
                }
                .flatMap { bookingSaved ->
                    logger.debug("[saveBooking] Processing containers and orders for bookingId=${bookingSaved.id}")

                    Mono.zip(
                        containerService.saveContainersForBooking(booking.containers, bookingSaved.id!!),
                        orderService.saveOrdersForBooking(booking.orders, bookingSaved.id)
                    ).flatMap { tuple ->
                        val containersSaved = tuple.t1
                        val ordersSaved = tuple.t2

                        logger.debug("[saveBooking] Saved ${containersSaved.size} containers and ${ordersSaved.size} orders")

                        // Create associations between orders and containers
                        associationService.createAssociations(
                            orders = ordersSaved,
                            containers = containersSaved,
                            bookingNumber = booking.bookingNumber
                        )
                        .thenReturn(bookingSaved.copy(containers = containersSaved, orders = ordersSaved))
                        .doOnSuccess {
                            logger.info("[saveBooking] Successfully completed booking orchestration for bookingNumber=${booking.bookingNumber}")
                        }
                    }
                }
            }
            .doOnError { error ->
                logger.error("[saveBooking] Error during booking orchestration for bookingNumber=${booking.bookingNumber}", error)
            }
    }

}
