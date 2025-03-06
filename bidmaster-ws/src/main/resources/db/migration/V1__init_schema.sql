
-- Creación de la tabla users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    status VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL,
    last_login TIMESTAMP,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0,
    blocked_balance DECIMAL(19, 2) NOT NULL DEFAULT 0,
    reputation INT NOT NULL DEFAULT 50,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);