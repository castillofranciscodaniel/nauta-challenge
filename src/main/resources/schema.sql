-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     create_at TIMESTAMP NOT NULL
);

-- Tabla de Bookings
CREATE TABLE IF NOT EXISTS bookings (
                                        id SERIAL PRIMARY KEY,
                                        booking_number VARCHAR(50) NOT NULL UNIQUE,
                                        usuario_id BIGINT NOT NULL,
                                        origen VARCHAR(100),
                                        destino VARCHAR(100),
                                        fecha_salida TIMESTAMP,
                                        fecha_llegada TIMESTAMP,
                                        estado VARCHAR(50),
                                        FOREIGN KEY (usuario_id) REFERENCES users(id)
);

-- Tabla de Containers
CREATE TABLE IF NOT EXISTS containers (
                                          id SERIAL PRIMARY KEY,
                                          container_number VARCHAR(50) NOT NULL UNIQUE,
                                          tipo VARCHAR(50),
                                          capacidad DECIMAL(10,2),
                                          estado VARCHAR(50)
);

-- Tabla de Orders
CREATE TABLE IF NOT EXISTS orders (
                                      id SERIAL PRIMARY KEY,
                                      purchase_number VARCHAR(50) NOT NULL UNIQUE,
                                      usuario_id BIGINT NOT NULL,
                                      fecha_compra TIMESTAMP,
                                      monto DECIMAL(10,2),
                                      estado VARCHAR(50),
                                      FOREIGN KEY (usuario_id) REFERENCES users(id)
);

-- Tabla de relación entre Bookings y Containers (si es necesaria)
CREATE TABLE IF NOT EXISTS booking_containers (
                                                  booking_id BIGINT NOT NULL,
                                                  container_id BIGINT NOT NULL,
                                                  PRIMARY KEY (booking_id, container_id),
                                                  FOREIGN KEY (booking_id) REFERENCES bookings(id),
                                                  FOREIGN KEY (container_id) REFERENCES containers(id)
);