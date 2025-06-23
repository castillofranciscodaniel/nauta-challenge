package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.BookingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import org.springframework.stereotype.Service

// ContainerService.kt
@Service
class ContainerService(
    private val containerRepository: ContainerRepository,
    private val userLoggedService: UserLoggedService,
    private val bookingRepository: BookingRepository
) {
    suspend fun saveContainersForBooking(containers: List<Container>, bookingId: Long): List<Container> {
        return containers.map { container ->
            containerRepository.findByContainerNumberAndBookingId(container.containerNumber, bookingId)
                ?: containerRepository.save(container.copy(bookingId = bookingId))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun findAllContainersForCurrentUser(): Flow<Container> {
        val currentUser = userLoggedService.getCurrentUserId()

        // Obtenemos todos los bookings del usuario
        return bookingRepository.findAllByUserId(currentUser.id!!)
            .flatMapMerge { booking ->
                // Para cada booking, obtenemos sus contenedores
                containerRepository.findAllByBookingId(booking.id!!)
            }
    }
}
