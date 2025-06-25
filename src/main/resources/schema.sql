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
                                        booking_number VARCHAR(50) NOT NULL,
                                        user_id BIGINT NOT NULL,
                                        UNIQUE (booking_number, user_id),
                                        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tabla de Containers
CREATE TABLE IF NOT EXISTS containers (
                                          id SERIAL PRIMARY KEY,
                                          container_number VARCHAR(50) NOT NULL,
                                          booking_id BIGINT NOT NULL,
                                          UNIQUE (container_number, booking_id),
                                          FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Tabla de Orders
CREATE TABLE IF NOT EXISTS orders (
                                      id SERIAL PRIMARY KEY,
                                      purchase_number VARCHAR(50) NOT NULL,
                                      booking_id BIGINT NOT NULL,
                                      UNIQUE (purchase_number, booking_id),
                                      FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Tabla de Invoice
CREATE TABLE IF NOT EXISTS invoices (
                                      id SERIAL PRIMARY KEY,
                                      invoice_number VARCHAR(50) NOT NULL,
                                      order_id BIGINT NOT NULL,
                                      UNIQUE (invoice_number, order_id),
                                      FOREIGN KEY (order_id) REFERENCES orders(id)
    );

-- Tabla de relación entre Bookings y Containers (si es necesaria)
CREATE TABLE IF NOT EXISTS order_containers (
                                                  id SERIAL PRIMARY KEY,
                                                  order_id BIGINT NOT NULL,
                                                  container_id BIGINT NOT NULL,
                                                  FOREIGN KEY (order_id) REFERENCES orders(id),
                                                  FOREIGN KEY (container_id) REFERENCES containers(id)
);