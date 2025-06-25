package com.challenge.nauta_challenge.infrastructure.consumer

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.service.BookingSaveOrchestrationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Componente responsable únicamente de consumir y procesar bookings fallidos desde Kafka
 */
@Component
class BookingKafkaConsumer(
    private val bookingSaveOrchestrationService: BookingSaveOrchestrationService,
    @Value("\${spring.kafka.failed-bookings.topic:failed-bookings}")
    val failedBookingsTopic: String, // Cambiado a público
    @Value("\${spring.kafka.failed-bookings.group:failed-bookings-group}")
    val failedBookingsGroup: String // Cambiado a público
) {
    private val logger = LoggerFactory.getLogger(BookingKafkaConsumer::class.java)

    /**
     * Escucha y procesa los bookings fallidos del tema Kafka
     */
    @KafkaListener(
        topics = ["#{__listener.failedBookingsTopic}"],
        groupId = "#{__listener.failedBookingsGroup}"
    )
    suspend fun consumeFailedBooking(booking: Booking) {
        val bookingNumber = booking.bookingNumber
        logger.info("[consumeFailedBooking] Recibido booking desde cola para reprocesamiento: {}", bookingNumber)

        try {
            val result = bookingSaveOrchestrationService.retryBookingSave(booking)
            logger.info(
                "[consumeFailedBooking] Booking reprocesado exitosamente: {}, ID generado: {}",
                bookingNumber, result.id
            )
        } catch (e: Exception) {
            logger.error(
                "[consumeFailedBooking] Error al reprocesar booking {}: {}",
                bookingNumber, e.message, e
            )
            // Manejar la excepción sin volver a lanzarla
        }
    }
}