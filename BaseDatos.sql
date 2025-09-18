-- Banking Database Schema
-- Created for microservices banking application

-- Create database
CREATE DATABASE IF NOT EXISTS banking_db;
USE banking_db;

-- Create personas table
CREATE TABLE IF NOT EXISTS personas (
    persona_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero CHAR(1) NOT NULL CHECK (genero IN ('M', 'F')),
    edad INT NOT NULL,
    identificacion VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(200) NOT NULL,
    telefono VARCHAR(10) NOT NULL CHECK (LENGTH(telefono) = 10)
);

-- Create clientes table (extends personas)
CREATE TABLE IF NOT EXISTS clientes (
    persona_id BIGINT NOT NULL,
    cliente_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contrasena VARCHAR(20) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (persona_id) REFERENCES personas(persona_id) ON DELETE CASCADE
);

-- Create cuentas table
CREATE TABLE IF NOT EXISTS cuentas (
    cuenta_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta VARCHAR(6) NOT NULL UNIQUE CHECK (LENGTH(numero_cuenta) = 6),
    tipo_cuenta VARCHAR(20) NOT NULL CHECK (tipo_cuenta IN ('Ahorro', 'Corriente', 'Ahorros')),
    saldo_inicial DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (saldo_inicial >= 0),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    cliente_id BIGINT NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(cliente_id) ON DELETE CASCADE
);

-- Create movimientos table
CREATE TABLE IF NOT EXISTS movimientos (
    movimiento_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('Deposito', 'Retiro')),
    valor DECIMAL(10,2) NOT NULL CHECK (valor > 0),
    saldo DECIMAL(10,2) NOT NULL,
    cuenta_id BIGINT NOT NULL,
    FOREIGN KEY (cuenta_id) REFERENCES cuentas(cuenta_id) ON DELETE CASCADE
);

-- Insert sample data for personas
INSERT INTO personas (nombre, genero, edad, identificacion, direccion, telefono) VALUES
('Jose Lema', 'M', 30, '1234567890', 'Otavalo sn y principal', '0982547856'),
('Marianela Montalvo', 'F', 25, '0987654321', 'Amazonas y NNUU', '0975489654'),
('Juan Osorio', 'M', 35, '1122334455', '13 junio y Equinoccial', '0988745876');

-- Insert sample data for clientes
INSERT INTO clientes (persona_id, contrasena, estado) VALUES
(1, '1234', TRUE),
(2, '5678', TRUE),
(3, '1245', TRUE);

-- Insert sample data for cuentas
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id) VALUES
('478758', 'Ahorro', 2000.00, TRUE, 1),
('225487', 'Corriente', 100.00, TRUE, 2),
('495878', 'Ahorros', 0.00, TRUE, 3),
('496825', 'Ahorros', 540.00, TRUE, 2),
('585545', 'Corriente', 1000.00, TRUE, 1);

-- Insert sample data for movimientos
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id) VALUES
('2024-02-10 10:30:00', 'Retiro', 575.00, 1425.00, 1),
('2024-02-10 11:15:00', 'Deposito', 600.00, 700.00, 2),
('2024-02-08 14:20:00', 'Deposito', 150.00, 150.00, 3),
('2024-02-08 16:45:00', 'Retiro', 540.00, 0.00, 4);

-- Create indexes for better performance
CREATE INDEX idx_personas_identificacion ON personas(identificacion);
CREATE INDEX idx_clientes_persona_id ON clientes(persona_id);
CREATE INDEX idx_cuentas_numero_cuenta ON cuentas(numero_cuenta);
CREATE INDEX idx_cuentas_cliente_id ON cuentas(cliente_id);
CREATE INDEX idx_movimientos_cuenta_id ON movimientos(cuenta_id);
CREATE INDEX idx_movimientos_fecha ON movimientos(fecha);

-- Create views for common queries
CREATE VIEW vista_clientes_completos AS
SELECT 
    c.cliente_id,
    p.nombre,
    p.genero,
    p.edad,
    p.identificacion,
    p.direccion,
    p.telefono,
    c.contrasena,
    c.estado
FROM clientes c
JOIN personas p ON c.persona_id = p.persona_id;

CREATE VIEW vista_cuentas_con_cliente AS
SELECT 
    cu.cuenta_id,
    cu.numero_cuenta,
    cu.tipo_cuenta,
    cu.saldo_inicial,
    cu.estado,
    cu.cliente_id,
    p.nombre as cliente_nombre
FROM cuentas cu
JOIN clientes cl ON cu.cliente_id = cl.cliente_id
JOIN personas p ON cl.persona_id = p.persona_id;

CREATE VIEW vista_movimientos_detallados AS
SELECT 
    m.movimiento_id,
    m.fecha,
    m.tipo_movimiento,
    m.valor,
    m.saldo,
    m.cuenta_id,
    cu.numero_cuenta,
    cu.tipo_cuenta,
    p.nombre as cliente_nombre
FROM movimientos m
JOIN cuentas cu ON m.cuenta_id = cu.cuenta_id
JOIN clientes cl ON cu.cliente_id = cl.cliente_id
JOIN personas p ON cl.persona_id = p.persona_id;

-- Create stored procedures for common operations
DELIMITER //

-- Procedure to get account balance
CREATE PROCEDURE GetAccountBalance(IN account_id BIGINT, OUT current_balance DECIMAL(10,2))
BEGIN
    DECLARE last_movement_balance DECIMAL(10,2) DEFAULT 0;
    DECLARE initial_balance DECIMAL(10,2) DEFAULT 0;
    
    -- Get initial balance
    SELECT saldo_inicial INTO initial_balance FROM cuentas WHERE cuenta_id = account_id;
    
    -- Get last movement balance
    SELECT saldo INTO last_movement_balance 
    FROM movimientos 
    WHERE cuenta_id = account_id 
    ORDER BY fecha DESC 
    LIMIT 1;
    
    -- Return current balance
    IF last_movement_balance IS NULL THEN
        SET current_balance = initial_balance;
    ELSE
        SET current_balance = last_movement_balance;
    END IF;
END //

-- Procedure to create movement with balance validation
CREATE PROCEDURE CreateMovementWithValidation(
    IN account_id BIGINT,
    IN movement_type VARCHAR(20),
    IN movement_value DECIMAL(10,2),
    OUT success BOOLEAN,
    OUT message VARCHAR(255)
)
BEGIN
    DECLARE current_balance DECIMAL(10,2) DEFAULT 0;
    DECLARE new_balance DECIMAL(10,2) DEFAULT 0;
    DECLARE account_exists BOOLEAN DEFAULT FALSE;
    
    -- Check if account exists and is active
    SELECT COUNT(*) > 0 INTO account_exists 
    FROM cuentas 
    WHERE cuenta_id = account_id AND estado = TRUE;
    
    IF NOT account_exists THEN
        SET success = FALSE;
        SET message = 'Account not found or inactive';
    ELSE
        -- Get current balance
        CALL GetAccountBalance(account_id, current_balance);
        
        -- Calculate new balance
        IF movement_type = 'Deposito' THEN
            SET new_balance = current_balance + movement_value;
        ELSEIF movement_type = 'Retiro' THEN
            SET new_balance = current_balance - movement_value;
            IF new_balance < 0 THEN
                SET success = FALSE;
                SET message = 'Saldo no disponible';
            ELSE
                SET success = TRUE;
                SET message = 'Movement created successfully';
            END IF;
        ELSE
            SET success = FALSE;
            SET message = 'Invalid movement type';
        END IF;
        
        -- Create movement if valid
        IF success THEN
            INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id)
            VALUES (NOW(), movement_type, movement_value, new_balance, account_id);
        END IF;
    END IF;
END //

DELIMITER ;

-- Grant permissions
GRANT ALL PRIVILEGES ON banking_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON banking_db.* TO 'banking_user'@'%';
FLUSH PRIVILEGES;

-- Show created tables
SHOW TABLES;

-- Show sample data
SELECT 'Personas' as table_name, COUNT(*) as record_count FROM personas
UNION ALL
SELECT 'Clientes', COUNT(*) FROM clientes
UNION ALL
SELECT 'Cuentas', COUNT(*) FROM cuentas
UNION ALL
SELECT 'Movimientos', COUNT(*) FROM movimientos;
