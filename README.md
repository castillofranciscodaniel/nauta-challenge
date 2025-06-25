# Nauta Challenge

Este proyecto es una API RESTful construida con Spring Boot y Kotlin que gestiona contenedores, órdenes de compra y reservas de embarque.

## Índice

1. [Cómo levantar el proyecto](#cómo-levantar-el-proyecto)
2. [Endpoints disponibles](#endpoints-disponibles)
   - [Autenticación](#autenticación)
   - [Bookings (Reservas)](#bookings-reservas)
   - [Orders (Órdenes de compra)](#orders-órdenes-de-compra)
   - [Containers (Contenedores)](#containers-contenedores)
3. [Testing y cobertura de código](#testing-y-cobertura-de-código)

## Cómo levantar el proyecto

### Opción 1: Docker Compose (todo incluido)

Esta opción levanta en un solo comando tanto la aplicación como la base de datos PostgreSQL y Kafka.

**Requisitos previos:**
- Docker
- Docker Compose

**Pasos:**

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/nauta-challenge.git
   cd nauta-challenge
   ```

2. Levanta todos los servicios con Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. La aplicación estará disponible en: `http://localhost:8080`

4. Para ver los logs:
   ```bash
   docker-compose logs -f
   ```

5. Para detener la aplicación:
   ```bash
   docker-compose down
   ```

### Opción 2: Kafka separado + Aplicación

Esta opción permite ejecutar Kafka en contenedores Docker mientras que la aplicación se ejecuta localmente.

**Requisitos previos:**
- Docker
- Docker Compose
- JDK 17
- Gradle

**Pasos:**

1. Levanta Kafka y PostgreSQL con Docker Compose específico para infraestructura:
   ```bash
   docker-compose -f kafka-docker-compose.yml up -d
   ```

2. Ejecuta la aplicación localmente:
   ```bash
   ./gradlew bootRun
   ```

3. Para detener los servicios de infraestructura:
   ```bash
   docker-compose -f kafka-docker-compose.yml down
   ```

### Opción 3: Ejecución completamente local

**Requisitos previos:**
- JDK 17
- Gradle
- PostgreSQL
- Kafka

**Pasos:**

1. Configura tu base de datos PostgreSQL local y ejecuta el script `schema.sql` ubicado en `src/main/resources/`.

2. Configura y ejecuta Kafka localmente siguiendo las [instrucciones oficiales](https://kafka.apache.org/quickstart).

3. Actualiza las configuraciones de conexión en `application.properties` si es necesario:
   ```properties
   # Configuración de PostgreSQL
   spring.r2dbc.url=r2dbc:postgresql://localhost:5432/nautadb
   spring.r2dbc.username=postgres
   spring.r2dbc.password=postgres
   
   # Configuración de Kafka
   spring.kafka.bootstrap-servers=localhost:9092
   spring.kafka.failed-bookings.topic=failed-bookings
   spring.kafka.failed-bookings.group=failed-bookings-group
   ```

4. Ejecuta el proyecto:
   ```bash
   ./gradlew bootRun
   ```

## Endpoints disponibles

### Autenticación

#### Registro de usuario
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@ejemplo.com",
    "password": "contraseña123"
  }'
```

#### Inicio de sesión
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@ejemplo.com",
    "password": "contraseña123"
  }'
```
Respuesta: Token JWT que debe usarse en el encabezado `Authorization: Bearer {token}` para las demás peticiones.

### Bookings 


#### Crear nuevo booking
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {tu-token}" \
  -d '{
    "booking": "BOOK123",
    "containers": [
      {
        "container": "CONT001"
      },
      {
        "container": "CONT002"
      }
    ],
    "orders": [
      {
        "purchase": "PO-001",
        "invoices": [
          {
            "invoice": "INV-001"
          }
        ]
      }
    ]
  }'
```

### Orders (Órdenes de compra)

#### Obtener todas las órdenes del usuario
```bash
curl -X GET http://localhost:8080/api/orders \
  -H "Authorization: Bearer {tu-token}"
```

#### Obtener contenedores asociados a una orden
```bash
curl -X GET http://localhost:8080/api/orders/{purchaseNumber}/containers \
  -H "Authorization: Bearer {tu-token}"
```

### Containers (Contenedores)

#### Obtener todos los contenedores del usuario
```bash
curl -X GET http://localhost:8080/api/containers \
  -H "Authorization: Bearer {tu-token}"
```

#### Obtener órdenes asociadas a un contenedor
```bash
curl -X GET http://localhost:8080/api/containers/{containerId}/orders \
  -H "Authorization: Bearer {tu-token}"
```

## Estructura del proyecto

El proyecto sigue una arquitectura hexagonal con:

- **Core**: Modelos de dominio y lógica de negocio
- **Adapters**: Implementaciones concretas de los repositorios
- **Infrastructure**: Controladores REST, seguridad y configuraciones

## Testing y cobertura de código

### Ejecución de pruebas

Para ejecutar las pruebas unitarias del proyecto:

```bash
./gradlew test
```

### Informe de cobertura de código

El proyecto está configurado con Jacoco para generar informes de cobertura de código.

Para ejecutar las pruebas y generar el informe de cobertura:

```bash
./gradlew test jacocoTestReport
```

O puedes utilizar el script incluido y se abrirá automáticamente el informe en tu navegador:

```bash
./run-coverage.sh
```

El informe de cobertura se generará en `build/reports/jacoco/test/html/index.html` y se abrirá automáticamente en tu navegador si usas el script `run-coverage.sh`.

### Notas importantes sobre las pruebas

- Las pruebas utilizan un perfil de configuración específico (`test`) con una base de datos H2 en memoria.
- Los archivos de configuración para las pruebas se encuentran en `src/test/resources/`.
- Para añadir nuevas pruebas, asegúrate de usar la configuración adecuada añadiendo la anotación `@ActiveProfiles("test")` a tus clases de prueba.

## Consideraciones técnicas

- El proyecto utiliza R2DBC para operaciones de base de datos reactivas
- Se implementa autenticación JWT para proteger los endpoints
- Se aplica programación reactiva con Kotlin Coroutines y Spring WebFlux
