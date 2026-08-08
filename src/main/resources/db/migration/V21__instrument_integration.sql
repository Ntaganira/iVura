-- =====================================================================
-- Instrument integration: devices, test maps, inbound messages, signoffs
-- + LabResult pipeline columns (source / accession / verification)
-- =====================================================================

CREATE TABLE analyzer_devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    model VARCHAR(100),
    interface_type VARCHAR(20) NOT NULL DEFAULT 'CSV',
    endpoint VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE analyzer_test_map (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES analyzer_devices(id) ON DELETE CASCADE,
    device_test_code VARCHAR(50) NOT NULL,
    catalog_id BIGINT NOT NULL REFERENCES lab_test_catalog(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (device_id, device_test_code)
);

CREATE TABLE instrument_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(100) NOT NULL UNIQUE,
    device_id BIGINT REFERENCES analyzer_devices(id),
    direction VARCHAR(10) NOT NULL DEFAULT 'INBOUND',
    payload TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    accession_number VARCHAR(50),
    patient_ref VARCHAR(100),
    error_code VARCHAR(30),
    error_detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_inst_msg_status ON instrument_messages(status);
CREATE INDEX idx_inst_msg_accession ON instrument_messages(accession_number);

CREATE TABLE result_signoffs (
    id BIGSERIAL PRIMARY KEY,
    lab_result_id BIGINT NOT NULL REFERENCES lab_results(id) ON DELETE CASCADE,
    signoff_user_id BIGINT REFERENCES users(id),
    action VARCHAR(20) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_signoffs_result ON result_signoffs(lab_result_id);

-- ---------------------------------------------------------------------
-- LabResult pipeline columns
-- ---------------------------------------------------------------------
ALTER TABLE lab_results ADD COLUMN source VARCHAR(10) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE lab_results ADD COLUMN accession_number VARCHAR(50);
ALTER TABLE lab_results ADD COLUMN device_id BIGINT REFERENCES analyzer_devices(id);
ALTER TABLE lab_results ADD COLUMN instrument_message_id BIGINT REFERENCES instrument_messages(id);
ALTER TABLE lab_results ADD COLUMN flag VARCHAR(10);
ALTER TABLE lab_results ADD COLUMN verified_by BIGINT REFERENCES users(id);
ALTER TABLE lab_results ADD COLUMN verified_at TIMESTAMP;
ALTER TABLE lab_results ADD COLUMN published_by BIGINT REFERENCES users(id);
ALTER TABLE lab_results ADD COLUMN published_at TIMESTAMP;
ALTER TABLE lab_results ADD COLUMN override_reason TEXT;

CREATE INDEX idx_lab_results_source ON lab_results(source);
CREATE INDEX idx_lab_results_accession ON lab_results(accession_number);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VERIFY_LAB',             'Verify Lab Results',       'Laboratory',      'VERIFY',   'Review and verify laboratory results'),
('APPROVE_CRITICAL_LAB',   'Approve Critical Results', 'Laboratory',      'APPROVE',  'Sign off critical / abnormal results'),
('CONFIGURE_DEVICES',      'Configure Analyzers',      'Lab Integration', 'MANAGE',   'Manage analyzer devices and test mappings'),
('VIEW_INTEGRATION_LOGS',  'View Integration Logs',    'Lab Integration', 'VIEW',     'View instrument message and error console');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module IN ('Laboratory', 'Lab Integration')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'DOCTOR' AND p.code IN ('VERIFY_LAB')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'NURSE' AND p.code IN ('VERIFY_LAB')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- Pages
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('LAB_WORKBENCH',  'Lab Verify',     'Laboratory',      '/lab-verify',     'check-square', NULL, 9),
('LAB_INTEGRATION', 'Lab Integration','Laboratory',     '/lab-integration', 'link',        NULL, 12);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'ADMIN' AND pg.code IN ('LAB_WORKBENCH', 'LAB_INTEGRATION')
ON CONFLICT DO NOTHING;

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code IN ('DOCTOR', 'NURSE') AND pg.code = 'LAB_WORKBENCH'
ON CONFLICT DO NOTHING;
