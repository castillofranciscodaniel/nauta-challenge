package com.challenge.nauta_challenge.infrastructure.security

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private lateinit var securityConfig: SecurityConfig


    @Test
    fun `passwordEncoder should be a BCryptPasswordEncoder`() {
        // Act
        val passwordEncoder = securityConfig.passwordEncoder()

        // Assert
        assertNotNull(passwordEncoder)

        // Verificamos que codifica correctamente
        val password = "password"
        val encodedPassword = passwordEncoder.encode(password)

        // Verificamos que no es el mismo texto plano
        assert(password != encodedPassword)

        // Verificamos que el match funciona
        assertTrue(passwordEncoder.matches(password, encodedPassword))
    }

    @Test
    fun `securityWebFilterChain should be configured correctly`(@Autowired serverHttpSecurity: org.springframework.security.config.web.server.ServerHttpSecurity) {
        // Act
        val filterChain = securityConfig.securityWebFilterChain(serverHttpSecurity)

        // Assert
        assertNotNull(filterChain)
        // Nota: Las propiedades internas del SecurityWebFilterChain son difíciles de probar directamente,
        // ya que muchas son privadas. Esta prueba verifica que la configuración no arroja excepciones.
    }
}
