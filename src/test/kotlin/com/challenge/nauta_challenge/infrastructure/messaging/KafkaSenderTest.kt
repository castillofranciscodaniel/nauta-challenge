package com.challenge.nauta_challenge.infrastructure.messaging

import com.challenge.nauta_challenge.core.model.Booking
import io.mockk.mockk
import io.mockk.verify
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
}

