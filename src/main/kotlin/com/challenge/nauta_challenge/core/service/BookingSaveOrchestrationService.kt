package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.BookingDeferredException
import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.repository.MessagePublisherRepository
import com.challenge.nauta_challenge.infrastructure.config.KafkaConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookingSaveOrchestrationService(
    private val bookingService: BookingService,
    private val userLoggedService: UserLoggedService,
    private val containerService: ContainerService,
    private val orderService: OrderService,
    private val associationService: OrderContainerAssociationService,
    private val messagePublisher: MessagePublisherRepository // Inyectamos el puerto en lugar del adaptador concreto
) {
    private val logger = LoggerFactory.getLogger(BookingSaveOrchestrationService::class.java)

    /**
     * Guarda un booking con manejo de resiliencia
     */
    suspend fun saveBooking(booking: Booking): Booking {
        logger.info("[saveBooking] Iniciando proceso de guardado de booking: {}", booking.bookingNumber)

        return try {
            processBookingSave(booking)
        } catch (e: Exception) {
            logger.error(
                "[saveBooking] Error while saving booking: {}, activando mecanismo de resiliencia",
                booking.bookingNumber, e
            )

            logger.info("[saveBooking] Enviando booking a cola de reprocesamiento: {}", booking.bookingNumber)
            val userId = booking.userId ?: userLoggedService.getCurrentUser().id
            val bookingWithUser = booking.copy(userId = userId)
            messagePublisher.publishMessage(
                topic = KafkaConfig.FAILED_BOOKINGS_TOPIC,
                message = bookingWithUser
            )

            // Lanzamos excepción personalizada para que el ControllerAdvice la capture
            throw BookingDeferredException(booking.bookingNumber)
        }
    }

    /**
     * Método interno para el procesamiento real del booking
     */
    private suspend fun processBookingSave(booking: Booking): Booking {
        val userId = runCatching { userLoggedService.getCurrentUser().id  }
            .getOrElse { booking.userId }

        // se lanza una excepcion de forma aleatoria para simular fallos en el proceso
        // con probabilidad 3 de 10
        if (Math.random() < 0.3) {
            throw RuntimeException("Simulated failure during booking save process")
        }

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

        return bookingSaved.copy(containers = containersSaved, orders = ordersSaved)
    }

    /**
     * Método especial para reintentar bookings fallidos desde Kafka
     */
    @Transactional
    suspend fun retryBookingSave(booking: Booking): Booking {
        logger.info("[retryBookingSave] Reintentando proceso de guardado para booking: {}", booking.bookingNumber)

        try {
            return processBookingSave(booking)
        } catch (e: Exception) {
            logger.error("[retryBookingSave] Reintento fallido para booking: {}", booking.bookingNumber, e)
            throw e
        }
    }
}