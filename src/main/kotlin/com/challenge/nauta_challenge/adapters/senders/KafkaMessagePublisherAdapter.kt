package com.challenge.nauta_challenge.adapters.senders

import com.challenge.nauta_challenge.core.repository.MessagePublisherRepository
import com.challenge.nauta_challenge.infrastructure.messaging.KafkaSender
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Adaptador que implementa el puerto MessagePublisherPort usando Kafka como
 * tecnología subyacente para la publicación de mensajes.
 */
@Component
class KafkaMessagePublisherAdapter(
    private val kafkaSender: KafkaSender
) : MessagePublisherRepository {

    private val logger = LoggerFactory.getLogger(KafkaMessagePublisherAdapter::class.java)

    /**
     * Implementación de la interfaz que usa Kafka para enviar mensajes
     */
    override suspend fun <T : Any> publishMessage(topic: String, message: T, messageKey: String?) {
        val key = messageKey ?: UUID.randomUUID().toString()
        logger.debug("[publishMessage] Enviando mensaje al tópico {} usando Kafka, key: {}", topic, key)

        kafkaSender.publishMessage(
            topic = topic,
            message = message,
            messageKey = key
        )
    }
}