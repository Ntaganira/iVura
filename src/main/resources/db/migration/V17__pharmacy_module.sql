-- =====================================================================
-- Pharmacy module: medicine inventory + dispensing
-- =====================================================================

CREATE TABLE medicines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    batch_number VARCHAR(50),
    manufacturer VARCHAR(100),
    dosage VARCHAR(50),
    unit VARCHAR(20),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER NOT NULL DEFAULT 10,
    unit_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    expiry_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_medicines_name ON medicines(name);

CREATE TABLE dispensations (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    medicine_id BIGINT NOT NULL REFERENCES medicines(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    dispensed_by BIGINT REFERENCES users(id),
    dispensed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note TEXT
);

CREATE INDEX idx_dispensations_patient ON dispensations(patient_id);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_MEDICINE',      'View Medicines',      'Pharmacy', 'VIEW',   'View medicine inventory'),
('MANAGE_MEDICINE',    'Manage Medicines',    'Pharmacy', 'MANAGE', 'Add, edit and adjust medicine stock'),
('DISPENSE_MEDICINE',  'Dispense Medicines',  'Pharmacy', 'CREATE', 'Dispense medicines to patients');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module = 'Pharmacy';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code IN ('DOCTOR', 'NURSE') AND p.code = 'VIEW_MEDICINE';

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code IN ('ADMIN', 'DOCTOR', 'NURSE') AND pg.code = 'PHARMACY'
ON CONFLICT DO NOTHING;
