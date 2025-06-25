package com.challenge.nauta_challenge.core.repository

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class MessagePublisherRepositoryTest {

    private val messagePublisherRepository: MessagePublisherRepository = mockk(relaxed = true)

    @Test
    fun `publishMessage envia mensaje al topico correcto`() = runTest {
        // Arrange
        val topic = "test-topic"
        val message = "test-message"

        // Act

        messagePublisherRepository.publishMessage(topic, message)


        // Assert
        coVerify { messagePublisherRepository.publishMessage(topic, message, null) }
    }
}
