package com.challenge.nauta_challenge.infrastructure.messaging

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Componente genérico responsable de enviar mensajes a cualquier tópico de Kafka
 */
@Component
class KafkaSender(
    private val kafkaMessageService: KafkaMessageService
) {
    private val logger = LoggerFactory.getLogger(KafkaSender::class.java)

    /**
     * Publica un mensaje en un tópico específico de Kafka
     *
     * @param topic El tópico de Kafka donde se enviará el mensaje
     * @param message El mensaje a enviar (puede ser cualquier objeto serializable)
     * @param messageKey Clave opcional para el mensaje (se genera automáticamente si no se proporciona)
     */
    suspend fun <T : Any> publishMessage(topic: String, message: T, messageKey: String = UUID.randomUUID().toString()) {
        logger.info("[publishMessage] Enviando mensaje al tópico {}, key: {}", topic, messageKey)

        try {
            kafkaMessageService.send(
                topic = topic,
                message = message,
                key = messageKey
            )

            logger.info("[publishMessage] Mensaje enviado exitosamente al tópico {}", topic)
        } catch (e: Exception) {
            logger.error("[publishMessage] Error inesperado al enviar mensaje al tópico {}: {}", topic, e.message, e)
        }
    }
}
