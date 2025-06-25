package com.challenge.nauta_challenge.infrastructure.config

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}")
    private val bootstrapServers: String,
    @Value("\${spring.kafka.failed-bookings.topic:failed-bookings}")
    private val failedBookingsTopic: String,
    @Value("\${spring.kafka.failed-bookings.group:failed-bookings-group}")
    private val failedBookingsGroup: String,
    @Value("\${spring.kafka.num-partitions:3}")
    private val numPartitions: Int,
    @Value("\${spring.kafka.replication-factor:1}")
    private val replicationFactor: Short
) {
    // Topic name constants
    companion object {
        // Estas propiedades ahora son propiedades calculadas que leen los valores inyectados
        @JvmStatic
        lateinit var FAILED_BOOKINGS_TOPIC: String
            private set

        @JvmStatic
        lateinit var FAILED_BOOKINGS_GROUP: String
            private set

        @JvmStatic
        var NUM_PARTITIONS: Int = 3
            private set

        @JvmStatic
        var REPLICATION_FACTOR: Short = 1
            private set
    }

    init {
        // Inicializamos los valores estáticos con los valores inyectados
        FAILED_BOOKINGS_TOPIC = failedBookingsTopic
        FAILED_BOOKINGS_GROUP = failedBookingsGroup
        NUM_PARTITIONS = numPartitions
        REPLICATION_FACTOR = replicationFactor
    }

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs = HashMap<String, Any>()
        configs[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        return KafkaAdmin(configs)
    }

    @Bean
    fun failedBookingsTopic(): NewTopic {
        return NewTopic(failedBookingsTopic, numPartitions, replicationFactor)
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps = HashMap<String, Any>()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        configProps[ProducerConfig.ACKS_CONFIG] = "all" // Confirmación más robusta
        configProps[ProducerConfig.RETRIES_CONFIG] = 3
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val props = HashMap<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = FAILED_BOOKINGS_GROUP
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[JsonDeserializer.TRUSTED_PACKAGES] = "*" // Confianza en todos los paquetes (ajustar en producción)
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}
