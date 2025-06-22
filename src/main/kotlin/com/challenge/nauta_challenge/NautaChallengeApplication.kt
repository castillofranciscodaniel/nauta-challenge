package com.challenge.nauta_challenge

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NautaChallengeApplication

fun main(args: Array<String>) {
	runApplication<NautaChallengeApplication>(*args) {
		webApplicationType = WebApplicationType.REACTIVE // opcional, por seguridad
	}
}
