package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import org.springframework.stereotype.Service

// ContainerService.kt
@Service
class ContainerService(private val containerRepository: ContainerRepository) {
    suspend fun saveContainersForBooking(containers: List<Container>, bookingId: Long): List<Container> {
        return containers.map { container ->
            containerRepository.findByContainerNumberAndBookingId(container.containerNumber, bookingId)
                ?: containerRepository.save(container.copy(bookingId = bookingId))
        }
    }
}
