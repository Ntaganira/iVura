-- =====================================================================
-- Department Management module + Specialization lookup
-- =====================================================================

-- ---------------------------------------------------------------------
-- Specializations (managed lookup doctors pick from)
-- ---------------------------------------------------------------------
CREATE TABLE specializations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

INSERT INTO specializations (name, description) VALUES
('Cardiology', 'Heart and cardiovascular system'),
('Pediatrics', 'Medical care for infants and children'),
('Orthopedics', 'Musculoskeletal system'),
('Neurology', 'Brain and nervous system'),
('General Medicine', 'Primary care and general health'),
('Emergency', 'Emergency medical services');

-- Doctors now pick a specialization from the lookup table
ALTER TABLE doctors ADD COLUMN specialization_id BIGINT REFERENCES specializations(id);

-- Backfill links from legacy free-text specializations where they match
UPDATE doctors d SET specialization_id = s.id
FROM specializations s
WHERE d.specialization IS NOT NULL AND LOWER(TRIM(d.specialization)) = LOWER(s.name);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_DEPARTMENT',      'View Departments',      'Department Management',     'VIEW',   'View the department list and details'),
('CREATE_DEPARTMENT',    'Create Department',     'Department Management',     'CREATE', 'Create a new department'),
('EDIT_DEPARTMENT',      'Edit Department',       'Department Management',     'EDIT',   'Update department information'),
('DELETE_DEPARTMENT',    'Delete Department',     'Department Management',     'DELETE', 'Delete a department'),
('VIEW_SPECIALIZATION',  'View Specializations',  'Specialization Management', 'VIEW',   'View the specialization list'),
('CREATE_SPECIALIZATION','Create Specialization', 'Specialization Management', 'CREATE', 'Create a new specialization'),
('EDIT_SPECIALIZATION',  'Edit Specialization',   'Specialization Management', 'EDIT',   'Update a specialization'),
('DELETE_SPECIALIZATION','Delete Specialization', 'Specialization Management', 'DELETE', 'Delete a specialization');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module IN ('Department Management', 'Specialization Management');

-- ---------------------------------------------------------------------
-- Pages
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('DEPARTMENTS',     'Departments',     'Departments',     '/departments',     'grid',   NULL, 14),
('SPECIALIZATIONS', 'Specializations', 'Specializations', '/specializations', 'layers', NULL, 15);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'ADMIN' AND pg.code IN ('DEPARTMENTS', 'SPECIALIZATIONS');
