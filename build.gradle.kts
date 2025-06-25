plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("jacoco")
}

group = "com.challenge"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
// Dependencias JJWT completas
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5") // Para soporte JSON
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:r2dbc-postgresql")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testImplementation("io.mockk:mockk:1.14.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Kafka dependencies
	implementation("org.springframework.kafka:spring-kafka")
	testImplementation("org.springframework.kafka:spring-kafka-test")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

// Configuración para Jacoco (cobertura de código)
jacoco {
    toolVersion = "0.8.11" // Versión de Jacoco
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/config/**",
                    "**/model/**",
                    "**/*Application*",
                    "**/*Configuration*",
                    "**/exception/**",
                    "**/dto/**"
                )
            }
        })
    )

    dependsOn(tasks.test)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // Generar reporte automáticamente después de ejecutar tests

    // Mostrar información detallada sobre los tests
	testLogging {
		events("passed", "skipped", "failed")
		showExceptions = true
		showCauses = true
		showStackTraces = true
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
	}

	// Configurar opciones de memoria y entorno para JVM
	jvmArgs(
		"-Xmx512m",
		"-XX:MaxMetaspaceSize=256m",
		"-Dspring.profiles.active=test"
	)

	// Habilitar salida estándar para depuración
	systemProperty("spring.test.constructor.autowire.mode", "all")
	systemProperty("junit.jupiter.execution.parallel.enabled", "false")

	// Asegurarse de que los tests se ejecuten cada vez
	outputs.upToDateWhen { false }
}

tasks.withType<Test> {
	useJUnitPlatform()
}
