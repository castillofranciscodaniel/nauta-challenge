package com.challenge.nauta_challenge.infrastructure.messaging

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Servicio genérico para enviar mensajes a Kafka de manera simplificada
 */
@Component
class KafkaMessageService(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(KafkaMessageService::class.java)

    /**
     * Envía un mensaje a un topic de Kafka de manera simplificada
     * 
     * @param topic El nombre del topic
     * @param message El mensaje a enviar
     * @param key La clave del mensaje (opcional, se genera automáticamente si no se proporciona)
     * @return CompletableFuture con el resultado del envío
     */
    fun send(topic: String, message: Any, key: String = UUID.randomUUID().toString()): CompletableFuture<SendResult<String, Any>> {
        logger.debug("[send] Enviando mensaje al topic {}, key: {}", topic, key)
        
        val future = kafkaTemplate.send(topic, key, message)
        
        future.whenComplete { result, ex ->
            if (ex == null) {
                logger.debug(
                    "[send] Mensaje enviado exitosamente. Topic: {}, Partition: {}, Offset: {}, Key: {}",
                    result.recordMetadata.topic(),
                    result.recordMetadata.partition(),
                    result.recordMetadata.offset(),
                    key
                )
            } else {
                logger.error("[send] Error enviando mensaje al topic {}, key: {}", topic, key, ex)
            }
        }
        
        return future
    }
}
