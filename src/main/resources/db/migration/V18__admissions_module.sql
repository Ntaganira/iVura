-- =====================================================================
-- Admissions module: ward rooms + patient room stays
-- =====================================================================

CREATE TABLE ward_rooms (
    id BIGSERIAL PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    ward_name VARCHAR(50),
    room_type VARCHAR(30) NOT NULL DEFAULT 'WARD',
    price_per_night DECIMAL(12, 2) NOT NULL DEFAULT 0,
    capacity INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE room_stays (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    room_id BIGINT NOT NULL REFERENCES ward_rooms(id),
    doctor_id BIGINT REFERENCES doctors(id),
    check_in_date DATE NOT NULL,
    check_out_date DATE,
    daily_rate DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ADMITTED',
    billing_id BIGINT REFERENCES billings(id),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_room_stays_patient ON room_stays(patient_id);
CREATE INDEX idx_room_stays_status ON room_stays(status);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_ROOM',           'View Rooms',         'Admissions', 'VIEW',   'View ward rooms and stays'),
('MANAGE_ROOM',         'Manage Rooms',       'Admissions', 'MANAGE', 'Add and edit ward rooms'),
('ADMIT_PATIENT',       'Admit Patient',      'Admissions', 'CREATE', 'Admit a patient to a room'),
('DISCHARGE_PATIENT',   'Discharge Patient',  'Admissions', 'EDIT',   'Discharge a patient and generate the bill');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module = 'Admissions';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'NURSE' AND p.code IN ('VIEW_ROOM', 'ADMIT_PATIENT', 'DISCHARGE_PATIENT');

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code IN ('ADMIN', 'NURSE') AND pg.code = 'ADMISSIONS'
ON CONFLICT DO NOTHING;
