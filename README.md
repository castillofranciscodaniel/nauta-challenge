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

1. Levanta Kafka con Docker Compose específico para infraestructura:
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

La estructura del proyecto sigue principios de arquitectura limpia (hexagonal) y está organizada de la siguiente manera:

```
├── build.gradle.kts                # Configuración de Gradle para el proyecto
├── docker-compose.yml              # Orquestación de servicios (app, base de datos, Kafka, etc.)
├── kafka-docker-compose.yml        # Orquestación alternativa solo para Kafka y dependencias
├── Dockerfile                      # Imagen Docker de la aplicación
├── src/
│   ├── main/
│   │   ├── kotlin/                 # Código fuente principal en Kotlin
│   │   │   └── com/
│   │   │       └── challenge/
│   │   │           └── nauta_challenge/
│   │   │               ├── core/           # Lógica de dominio, puertos y modelos
│   │   │               ├── infrastructure/  # Adaptadores, controladores, config, mensajería, advice
│   │   │               └── ...
│   │   └── resources/              # Archivos de configuración y recursos estáticos
│   │       ├── application.properties
│   │       ├── schema.sql
│   │       └── ...
│   └── test/
│       ├── kotlin/                 # Pruebas unitarias y de integración
│       └── resources/              # Configuración específica para tests
├── build/                          # Archivos generados por la compilación y ejecución
├── run-coverage.sh                 # Script para generar y abrir el reporte de cobertura
└── README.md                       # Este archivo
```

### Detalle de carpetas principales

- **adapters/**: Contiene los adaptadores de BD, clientes HTTP (si hubiera), sender Kafka y cualquier otro adaptador necesario para la comunicación con tecnologías externas. Estos adaptadores implementan los puertos definidos en el núcleo del proyecto.
- **core/**: Lógica de negocio, modelos de dominio, puertos (interfaces) y excepciones personalizadas. Aquí no hay dependencias de frameworks externos.
- **infrastructure/**: Incluye controladores REST, configuración de Spring, advice global de excepciones y cualquier integración transversal o de infraestructura general (seguridad, configuración, etc). 
- **resources/**: Archivos de configuración (`application.properties`), scripts de base de datos (`schema.sql`), y otros recursos estáticos.
- **test/**: Pruebas unitarias y de integración, junto con configuraciones específicas para el entorno de test.
- **build/**: Carpeta generada automáticamente por Gradle, contiene los resultados de la compilación, reportes y archivos temporales.

Esta organización facilita la mantenibilidad, escalabilidad y testabilidad del proyecto, siguiendo buenas prácticas de separación de responsabilidades y arquitectura limpia.

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

## Simulación de fallos en el guardado de bookings

Para facilitar pruebas de resiliencia y reprocesamiento, el método interno `processBookingSave` en el servicio `BookingSaveOrchestrationService` incluye una simulación de fallos aleatorios:

```kotlin
private suspend fun processBookingSave(booking: Booking): Booking {
    val userId = runCatching { userLoggedService.getCurrentUser().id }
        .getOrElse { booking.userId }
    // se lanza una excepcion de forma aleatoria para simular fallos en el proceso
    // con probabilidad 3 de 10
    if (Math.random() < 0.3) {
        throw RuntimeException("Simulated failure during booking save process")
    }
    // ...
}
```

**¿Qué significa esto?**
- Cada vez que se intenta guardar un booking, existe un 30% de probabilidad de que se lance una excepción simulando un fallo en el proceso.
- Esto permite probar el flujo de resiliencia: cuando ocurre un fallo, el booking se envía a Kafka para su reprocesamiento automático.
- El método también obtiene el `userId` del usuario logueado si está disponible; si no, utiliza el `userId` incluido en el propio objeto `Booking` (útil para reprocesos asíncronos desde Kafka, donde no hay contexto de usuario).

Esta lógica es útil para validar que el sistema maneja correctamente los errores y reprocesa los mensajes fallidos, asegurando la robustez de la solución ante fallos inesperados.

### Visualización de mensajes con Kafka UI (interfaz web)

El proyecto incluye el servicio `kafka-ui` en el `docker-compose.yml`, que te permite visualizar y explorar los mensajes de Kafka desde una interfaz web muy sencilla.

- Una vez que levantes los servicios con Docker Compose, accede a la UI en:

  [http://localhost:8081](http://localhost:8081)

- Desde allí podrás:
  - Navegar los topics (incluyendo la DLQ si la configuras)
  - **Ver los mensajes de cada topic** (por ejemplo, los mensajes del topic `failed-bookings` en:
    [http://localhost:8081/ui/clusters/local/all-topics/failed-bookings/messages](http://localhost:8081/ui/clusters/local/all-topics/failed-bookings/messages?keySerde=String&valueSerde=String&limit=100))
  - Ver particiones y offsets
  - Buscar y filtrar mensajes
  - Ver el estado del clúster y los consumidores

Esta herramienta es muy útil para depuración y monitoreo durante el desarrollo.
