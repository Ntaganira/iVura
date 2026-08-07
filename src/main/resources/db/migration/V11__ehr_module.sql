-- =====================================================================
-- EHR / Clinical module: lab results and immunizations
-- =====================================================================

CREATE TABLE lab_results (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    doctor_id BIGINT REFERENCES doctors(id),
    test_name VARCHAR(150) NOT NULL,
    category VARCHAR(50),
    result TEXT,
    unit VARCHAR(30),
    normal_range VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    performed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_lab_results_patient ON lab_results(patient_id);
CREATE INDEX idx_lab_results_status ON lab_results(status);

CREATE TABLE immunizations (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    vaccine VARCHAR(150) NOT NULL,
    dose_number INTEGER DEFAULT 1,
    administered_date DATE NOT NULL,
    next_due_date DATE,
    administered_by BIGINT REFERENCES doctors(id),
    batch_number VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_immunizations_patient ON immunizations(patient_id);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_RECORD',       'View Medical Records',      'EHR / Medical Records', 'VIEW',   'View patient medical records'),
('CREATE_RECORD',     'Create Medical Record',     'EHR / Medical Records', 'CREATE', 'Record a diagnosis, prescription or note'),
('EDIT_RECORD',       'Edit Medical Record',       'EHR / Medical Records', 'EDIT',   'Update a medical record'),
('DELETE_RECORD',     'Delete Medical Record',     'EHR / Medical Records', 'DELETE', 'Delete a medical record'),
('VIEW_LAB',          'View Lab Results',          'Laboratory',            'VIEW',   'View laboratory results'),
('CREATE_LAB',        'Create Lab Result',         'Laboratory',            'CREATE', 'Record a laboratory result'),
('EDIT_LAB',          'Update Lab Status',         'Laboratory',            'EDIT',   'Update laboratory result status'),
('VIEW_IMMUNIZATION', 'View Immunizations',        'Immunization',           'VIEW',   'View immunization records'),
('CREATE_IMMUNIZATION','Create Immunization',      'Immunization',           'CREATE', 'Record a vaccination');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module IN ('EHR / Medical Records', 'Laboratory', 'Immunization');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'DOCTOR' AND p.code IN (
    'VIEW_RECORD','CREATE_RECORD','EDIT_RECORD',
    'VIEW_LAB','CREATE_LAB','EDIT_LAB',
    'VIEW_IMMUNIZATION','CREATE_IMMUNIZATION'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'NURSE' AND p.code IN (
    'VIEW_RECORD','CREATE_RECORD',
    'VIEW_LAB','CREATE_LAB',
    'VIEW_IMMUNIZATION','CREATE_IMMUNIZATION'
);

-- ---------------------------------------------------------------------
-- Pages (LABORATORY already exists from V2, code 15)
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('MEDICAL_RECORDS', 'Medical Records', 'EHR', '/medical-records', 'file-text', NULL, 8);

UPDATE pages SET sort_order = sort_order + 1
WHERE code IN ('LABORATORY', 'BILLING', 'INSURANCE', 'REPORTS');

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'ADMIN' AND pg.code IN ('MEDICAL_RECORDS', 'LABORATORY')
ON CONFLICT DO NOTHING;

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code IN ('DOCTOR', 'NURSE') AND pg.code IN ('MEDICAL_RECORDS', 'LABORATORY')
ON CONFLICT DO NOTHING;
