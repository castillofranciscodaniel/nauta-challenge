package com.challenge.nauta_challenge.core.repository

/**
 * Puerto de salida (output port) para publicar mensajes.
 * Define un contrato independiente de la infraestructura concreta para enviar mensajes.
 */
interface MessagePublisherRepository {
    /**
     * Publica un mensaje en el sistema de mensajería.
     *
     * @param T El tipo del mensaje a publicar
     * @param topic El nombre del tópico o canal donde se publicará el mensaje
     * @param message El mensaje a publicar
     * @param messageKey Clave opcional para el mensaje (se genera automáticamente si no se proporciona)
     */
    suspend fun <T : Any> publishMessage(topic: String, message: T, messageKey: String? = null)
}