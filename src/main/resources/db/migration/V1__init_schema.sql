CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    phone VARCHAR(20),
    location VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    specialization VARCHAR(100),
    license_number VARCHAR(50) UNIQUE,
    department_id BIGINT REFERENCES departments(id),
    qualification TEXT,
    experience_years INTEGER DEFAULT 0,
    consultation_fee DECIMAL(10,2) DEFAULT 0,
    available_from TIME DEFAULT '09:00',
    available_to TIME DEFAULT '17:00',
    is_active BOOLEAN DEFAULT TRUE,
    photo_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    blood_group VARCHAR(5),
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(20),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    medical_history TEXT,
    allergies TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    photo_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW'))
);

CREATE TABLE medical_records (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    appointment_id BIGINT REFERENCES appointments(id),
    diagnosis TEXT,
    prescription TEXT,
    notes TEXT,
    record_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE billings (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    appointment_id BIGINT REFERENCES appointments(id),
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) DEFAULT 0,
    discount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_date TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_billing_status CHECK (status IN ('PENDING', 'PAID', 'PARTIALLY_PAID', 'CANCELLED', 'REFUNDED'))
);

-- Seed data
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_DOCTOR'), ('ROLE_NURSE'), ('ROLE_STAFF');

-- Default admin password: password123 (BCrypt, strength 12)
INSERT INTO users (username, email, password, full_name, phone) VALUES
('admin', 'admin@ivura.com', '$2a$12$YkjpDkoLx5VZxafkfUdG6.J1W6B7VnXyAzyGZ0HNzIv1PFDxG2gZm', 'System Admin', '+250780000000');

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

INSERT INTO departments (name, description, phone, location) VALUES
('Cardiology', 'Heart and cardiovascular system', '+250781111111', 'Floor 1 - East Wing'),
('Pediatrics', 'Medical care for infants and children', '+250781111112', 'Floor 2 - West Wing'),
('Orthopedics', 'Musculoskeletal system', '+250781111113', 'Floor 1 - West Wing'),
('Neurology', 'Brain and nervous system', '+250781111114', 'Floor 3 - East Wing'),
('General Medicine', 'Primary care and general health', '+250781111115', 'Ground Floor - Main'),
('Emergency', 'Emergency medical services', '+250781111116', 'Ground Floor - Emergency Wing');
