package com.challenge.nauta_challenge.infrastructure.messaging

import com.challenge.nauta_challenge.core.model.Booking
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.*

class KafkaSenderTest {
    private val kafkaMessageService = mockk<KafkaMessageService>(relaxed = true)
    private val kafkaSender = KafkaSender(kafkaMessageService)

    @Test
    fun `publishMessage llama a kafkaMessageService con los parametros correctos`() = runTest {
        val booking = Booking(bookingNumber = "B1", userId = 1, containers = emptyList(), orders = emptyList())
        val topic = "test-topic"
        val key = UUID.randomUUID().toString()

        kafkaSender.publishMessage(topic, booking, key)

        verify { kafkaMessageService.send(topic, booking, key) }
    }

    @Test
    fun `publishMessage maneja excepcion correctamente`() = runTest {
        val booking = Booking(bookingNumber = "B1", userId = 1, containers = emptyList(), orders = emptyList())
        val topic = "test-topic"
        val key = UUID.randomUUID().toString()

        // Simular una excepción al enviar el mensaje
        coEvery { kafkaMessageService.send(topic, booking, key) } throws Exception("Simulated exception")

        // Act
        kafkaSender.publishMessage(topic, booking, key)

        // Assert
        coVerify(exactly = 1) { kafkaMessageService.send(topic, booking, key) }
        // Verificar que no se propaga la excepción
    }
}
