-- =====================================================================
-- Separate Specializations (medical expertise) from Services (hospital catalog)
-- Specialization  = what the doctor is qualified in (no pricing)
-- Service         = what the hospital offers and bills
-- Doctor <-> Service assignment = what the doctor can provide
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Specializations lookup (medical expertise, no pricing/billing)
-- ---------------------------------------------------------------------
CREATE TABLE specializations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Seed from existing catalog rows (they were expertise areas)
INSERT INTO specializations (name, description)
SELECT name, description FROM services
ON CONFLICT (name) DO NOTHING;

INSERT INTO specializations (name, description) VALUES
('Dermatology', 'Skin, hair and nails'),
('Internal Medicine', 'Prevention, diagnosis and treatment of adult diseases')
ON CONFLICT (name) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. Extend the services catalog with operational/financial attributes
-- ---------------------------------------------------------------------
ALTER TABLE services ADD COLUMN department_id BIGINT REFERENCES departments(id);
ALTER TABLE services ADD COLUMN category VARCHAR(100);
ALTER TABLE services ADD COLUMN insurance_code VARCHAR(50);
ALTER TABLE services ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Link existing services to their department where names match
UPDATE services s SET department_id = d.id
FROM departments d
WHERE s.department_id IS NULL AND LOWER(TRIM(s.name)) = LOWER(TRIM(d.name));

INSERT INTO services (name, description, price, department_id, category, insurance_code) VALUES
('General Consultation',    'General consultation with a doctor',             10000, (SELECT id FROM departments WHERE name = 'General Medicine'), 'Consultation', 'GEN-CONS'),
('Cardiology Consultation', 'Heart and cardiovascular consultation',           20000, (SELECT id FROM departments WHERE name = 'Cardiology'),      'Consultation', 'CARD-CONS'),
('ECG Examination',         'Electrocardiogram examination',                   15000, (SELECT id FROM departments WHERE name = 'Cardiology'),      'Diagnostic',   'CARD-ECG'),
('Ultrasound',              'Ultrasound imaging examination',                  25000, (SELECT id FROM departments WHERE name = 'Cardiology'),      'Imaging',      'IMG-US'),
('Laboratory Test',         'Standard laboratory testing',                      12000, (SELECT id FROM departments WHERE name = 'General Medicine'), 'Laboratory',   'LAB-STD'),
('Minor Surgery',           'Minor outpatient surgical procedure',              50000, (SELECT id FROM departments WHERE name = 'Emergency'),       'Procedure',    'SURG-MIN')
ON CONFLICT (name) DO NOTHING;

-- ---------------------------------------------------------------------
-- 3. Doctor <-> Specialization and Doctor <-> Service (many-to-many)
-- ---------------------------------------------------------------------
CREATE TABLE doctor_specializations (
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    specialization_id BIGINT NOT NULL REFERENCES specializations(id) ON DELETE CASCADE,
    PRIMARY KEY (doctor_id, specialization_id)
);

CREATE TABLE doctor_services (
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    PRIMARY KEY (doctor_id, service_id)
);

-- Backfill from the legacy single specialization/service link
INSERT INTO doctor_specializations (doctor_id, specialization_id)
SELECT d.id, s.id
FROM doctors d
JOIN services sv ON sv.id = d.service_id
JOIN specializations s ON LOWER(TRIM(s.name)) = LOWER(TRIM(sv.name))
ON CONFLICT DO NOTHING;

INSERT INTO doctor_services (doctor_id, service_id)
SELECT id, service_id FROM doctors WHERE service_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- 4. Remove doctor-level pricing, single specialization link and legacy
--    free-text specialization column
-- ---------------------------------------------------------------------
ALTER TABLE doctors DROP COLUMN service_id;
ALTER TABLE doctors DROP COLUMN consultation_fee;
ALTER TABLE doctors DROP COLUMN specialization;

-- ---------------------------------------------------------------------
-- 5. Appointment references the selected service (billing basis)
-- ---------------------------------------------------------------------
ALTER TABLE appointments ADD COLUMN service_id BIGINT REFERENCES services(id);

-- ---------------------------------------------------------------------
-- 6. Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_SPECIALIZATION',   'View Specializations',   'Specialization Management', 'VIEW',   'View the specialization list'),
('CREATE_SPECIALIZATION', 'Create Specialization',  'Specialization Management', 'CREATE', 'Create a new specialization'),
('EDIT_SPECIALIZATION',   'Edit Specialization',    'Specialization Management', 'EDIT',   'Update a specialization'),
('DELETE_SPECIALIZATION', 'Delete Specialization',  'Specialization Management', 'DELETE', 'Delete a specialization');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module = 'Specialization Management';

-- ---------------------------------------------------------------------
-- 7. Pages
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('SPECIALIZATIONS', 'Specializations', 'Specializations', '/specializations', 'layers', NULL, 16);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'ADMIN' AND pg.code = 'SPECIALIZATIONS';
