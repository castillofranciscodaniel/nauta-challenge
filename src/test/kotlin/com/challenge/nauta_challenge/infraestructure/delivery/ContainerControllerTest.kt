package com.challenge.nauta_challenge.infraestructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.service.ContainerService
import com.challenge.nauta_challenge.infrastructure.delivery.ContainerController
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class ContainerControllerTest {

    private val containerService = mockk<ContainerService>()
    private val containerController = ContainerController(containerService)

    @Test
    fun `getAllContainers should return flow of containers from service`() = runTest {
        // Given
        val container1 = Container(id = 1L, containerNumber = "CONT-001", bookingId = 100L)
        val container2 = Container(id = 2L, containerNumber = "CONT-002", bookingId = 100L)
        val containersFlow: Flow<Container> = flowOf(container1, container2)

        coEvery { containerService.findAllContainersForCurrentUser() }.returns(containersFlow)

        // When
        val result = containerController.getAllContainers()

        // Then
        val resultList = result.toList()
        assertEquals(2, resultList.size)
        assertEquals(container1.id, resultList[0].id)
        assertEquals(container1.containerNumber, resultList[0].containerNumber)
        assertEquals(container2.id, resultList[1].id)
        assertEquals(container2.containerNumber, resultList[1].containerNumber)
    }

    @Test
    fun `getAllContainers should return empty flow when no containers exist`() = runTest {
        // Given
        val emptyContainersFlow: Flow<Container> = flowOf()

        coEvery { containerService.findAllContainersForCurrentUser() }.returns(emptyContainersFlow)

        // When
        val result = containerController.getAllContainers()

        // Then
        val resultList = result.toList()
        assertEquals(0, resultList.size)
    }
}
