-- =====================================================================
-- Radiology / Imaging: exam catalog, orders (accession) and reports
-- =====================================================================

CREATE TABLE radiology_exams (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    modality VARCHAR(30),
    body_part VARCHAR(50),
    price DECIMAL(14, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_rad_exams_modality ON radiology_exams(modality);

CREATE TABLE radiology_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    accession_number VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    doctor_id BIGINT REFERENCES doctors(id),
    priority VARCHAR(20) NOT NULL DEFAULT 'ROUTINE',
    status VARCHAR(30) NOT NULL DEFAULT 'ORDERED',
    ordered_by BIGINT REFERENCES users(id),
    notes TEXT,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_rad_orders_patient ON radiology_orders(patient_id);
CREATE INDEX idx_rad_orders_status ON radiology_orders(status);
CREATE INDEX idx_rad_orders_accession ON radiology_orders(accession_number);

CREATE TABLE radiology_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES radiology_orders(id) ON DELETE CASCADE,
    exam_id BIGINT REFERENCES radiology_exams(id),
    exam_name VARCHAR(150) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ORDERED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rad_items_order ON radiology_order_items(order_id);

CREATE TABLE radiology_reports (
    id BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT NOT NULL UNIQUE REFERENCES radiology_order_items(id) ON DELETE CASCADE,
    findings TEXT,
    impression TEXT,
    clinical_history TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    reported_by BIGINT REFERENCES users(id),
    reported_at TIMESTAMP,
    verified_by BIGINT REFERENCES users(id),
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_rad_reports_status ON radiology_reports(status);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_RAD_CATALOG',   'View Radiology Catalog',   'Radiology', 'VIEW',    'View the radiology exam catalog'),
('MANAGE_RAD_CATALOG', 'Manage Radiology Catalog', 'Radiology', 'MANAGE',  'Add and edit catalog exams'),
('VIEW_RAD_ORDER',     'View Radiology Orders',    'Radiology', 'VIEW',    'View radiology orders'),
('CREATE_RAD_ORDER',   'Create Radiology Orders',  'Radiology', 'CREATE',  'Place a radiology order'),
('MANAGE_RAD_ORDER',   'Manage Radiology Orders',  'Radiology', 'MANAGE',  'Cancel orders and manage workflow'),
('PERFORM_RAD_EXAM',   'Perform Radiology Exams',  'Radiology', 'PERFORM', 'Mark exams as performed (images captured)'),
('WRITE_RAD_REPORT',   'Write Radiology Reports',  'Radiology', 'REPORT',  'Enter findings and impression for an exam'),
('VERIFY_RAD_REPORT',  'Verify Radiology Reports', 'Radiology', 'VERIFY',  'Verify and sign off radiology reports');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module = 'Radiology'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'DOCTOR' AND p.code IN
      ('VIEW_RAD_CATALOG', 'VIEW_RAD_ORDER', 'CREATE_RAD_ORDER', 'WRITE_RAD_REPORT', 'VERIFY_RAD_REPORT')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'NURSE' AND p.code IN
      ('VIEW_RAD_CATALOG', 'VIEW_RAD_ORDER', 'CREATE_RAD_ORDER', 'MANAGE_RAD_ORDER', 'PERFORM_RAD_EXAM')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- Pages
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('RAD_ORDERS',   'Radiology Orders',   'Radiology', '/radiology-orders',   'list',     NULL, 13),
('RAD_WORKBENCH','Radiology Reporting','Radiology', '/radiology-workbench','edit',     NULL, 14),
('RAD_CATALOG',  'Radiology Catalog',  'Radiology', '/radiology-catalog',  'scanner',  NULL, 15);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'ADMIN' AND pg.code IN ('RAD_ORDERS', 'RAD_WORKBENCH', 'RAD_CATALOG')
ON CONFLICT DO NOTHING;

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code IN ('DOCTOR', 'NURSE') AND pg.code IN ('RAD_ORDERS', 'RAD_CATALOG')
ON CONFLICT DO NOTHING;

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'DOCTOR' AND pg.code = 'RAD_WORKBENCH'
ON CONFLICT DO NOTHING;
