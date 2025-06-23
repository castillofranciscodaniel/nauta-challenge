package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.ContainerService
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ContainerController(
    private val containerService: ContainerService
) {

    @GetMapping("/containers")
    suspend fun getAllContainers(): Flow<Container> {
        return containerService.findAllContainersForCurrentUser()
    }

    @GetMapping("/containers/{containerId}/orders")
    suspend fun getOrdersByContainerId(@PathVariable containerId: String): Flow<Order> {
        return containerService.findOrdersByContainerId(containerId)
    }
}
