package com.challenge.nauta_challenge.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.test.context.ActiveProfiles

@TestConfiguration
@EnableWebFluxSecurity
@ActiveProfiles("test")
class TestConfig {

    @Bean
    @Primary
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        return initializer
    }
}
