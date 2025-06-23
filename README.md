# Nauta Challenge

Este proyecto es una API RESTful construida con Spring Boot y Kotlin que gestiona contenedores, órdenes de compra y reservas de embarque.

## Índice

1. [Cómo levantar el proyecto](#cómo-levantar-el-proyecto)
2. [Endpoints disponibles](#endpoints-disponibles)
   - [Autenticación](#autenticación)
   - [Bookings (Reservas)](#bookings-reservas)
   - [Orders (Órdenes de compra)](#orders-órdenes-de-compra)
   - [Containers (Contenedores)](#containers-contenedores)

## Cómo levantar el proyecto

### Opción 1: Docker Compose (recomendado)

El proyecto incluye Docker y Docker Compose para levantar tanto la aplicación como la base de datos PostgreSQL.

**Requisitos previos:**
- Docker
- Docker Compose

**Pasos:**

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/nauta-challenge.git
   cd nauta-challenge
   ```

2. Levanta los contenedores:
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

### Opción 2: Ejecución local

**Requisitos previos:**
- JDK 17
- Gradle
- PostgreSQL

**Pasos:**

1. Configura tu base de datos PostgreSQL local y ejecuta el script `schema.sql` ubicado en `src/main/resources/`.

2. Actualiza las configuraciones de conexión en `application.properties` si es necesario.

3. Ejecuta el proyecto:
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

## Tests

Para ejecutar los tests:

```bash
./gradlew test
```

## Consideraciones técnicas

- El proyecto utiliza R2DBC para operaciones de base de datos reactivas
- Se implementa autenticación JWT para proteger los endpoints
- Se aplica programación reactiva con Kotlin Coroutines y Spring WebFlux

