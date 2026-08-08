-- =====================================================================
-- Insurance Claims module + patient insurance details
-- =====================================================================

-- Patient insurance details
ALTER TABLE patients ADD COLUMN insurance_provider VARCHAR(100);
ALTER TABLE patients ADD COLUMN insurance_policy_number VARCHAR(50);
ALTER TABLE patients ADD COLUMN insurance_member_name VARCHAR(100);
ALTER TABLE patients ADD COLUMN insurance_expiry_date DATE;
ALTER TABLE patients ADD COLUMN has_insurance BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE insurance_claims (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    billing_id BIGINT REFERENCES billings(id),
    provider VARCHAR(100) NOT NULL,
    policy_number VARCHAR(50),
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_date DATE NOT NULL,
    decision_date DATE,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_insurance_claims_patient ON insurance_claims(patient_id);
CREATE INDEX idx_insurance_claims_status ON insurance_claims(status);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_CLAIM',      'View Claims',         'Insurance', 'VIEW',   'View insurance claims'),
('CREATE_CLAIM',    'Create Claims',       'Insurance', 'CREATE', 'Submit an insurance claim'),
('EDIT_CLAIM',      'Edit Claims',         'Insurance', 'EDIT',   'Update claim details'),
('APPROVE_CLAIM',   'Approve Claims',      'Insurance', 'APPROVE', 'Approve or reject a claim');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module = 'Insurance';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'NURSE' AND p.code IN ('VIEW_CLAIM', 'CREATE_CLAIM');

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code IN ('ADMIN', 'NURSE') AND pg.code = 'INSURANCE'
ON CONFLICT DO NOTHING;
