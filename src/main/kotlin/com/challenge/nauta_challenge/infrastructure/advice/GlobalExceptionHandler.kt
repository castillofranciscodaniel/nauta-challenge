package com.challenge.nauta_challenge.infrastructure.advice

import com.challenge.nauta_challenge.core.exception.BookingDeferredException
import com.challenge.nauta_challenge.core.exception.ModelNotSavedException
import com.challenge.nauta_challenge.core.exception.RepositoryException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BookingDeferredException::class)
    fun handleBookingDeferredException(
        ex: BookingDeferredException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val body: Map<String, Any> = mapOf(
            "message" to (ex.message ?: "El booking no pudo ser procesado en este momento y será reprocesado automáticamente."),
            "bookingNumber" to ex.bookingNumber,
            "status" to 202
        )
        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).body(body))
    }

    @ExceptionHandler(ModelNotSavedException::class)
    fun handleModelNotSavedException(
        ex: ModelNotSavedException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val body: Map<String, Any> = mapOf(
            "message" to (ex.message ?: "No se pudo guardar el modelo en la base de datos."),
            "status" to 500
        )
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body))
    }

    @ExceptionHandler(RepositoryException::class)
    fun handleRepositoryException(
        ex: RepositoryException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val body: Map<String, Any> = mapOf(
            "message" to (ex.message ?: "Error de repositorio."),
            "status" to 500
        )
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body))
    }

    // Ejemplo de otra excepción personalizada
    // Puedes agregar más ExceptionHandler aquí para tus otras excepciones de dominio
    // @ExceptionHandler(MiOtraExcepcion::class)
    // fun handleMiOtraExcepcion(...): Mono<ResponseEntity<...>> { ... }

    // Manejo general para cualquier excepción no controlada
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val body = mapOf(
            "message" to (ex.message ?: "Error interno del servidor"),
            "status" to 500
        )
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body))
    }
}
