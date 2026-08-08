-- =====================================================================
-- Lab Catalog + Lab Orders (accession/specimen) + permissions
-- =====================================================================

CREATE TABLE lab_test_catalog (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50),
    specimen_type VARCHAR(50),
    unit VARCHAR(30),
    normal_range VARCHAR(100),
    ref_low DECIMAL(14, 4),
    ref_high DECIMAL(14, 4),
    critical_low DECIMAL(14, 4),
    critical_high DECIMAL(14, 4),
    delta_threshold DECIMAL(14, 4),
    auto_verify_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_lab_catalog_category ON lab_test_catalog(category);

CREATE TABLE lab_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    accession_number VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    doctor_id BIGINT REFERENCES doctors(id),
    priority VARCHAR(20) NOT NULL DEFAULT 'ROUTINE',
    status VARCHAR(30) NOT NULL DEFAULT 'ORDERED',
    specimen_type VARCHAR(50),
    specimen_received_at TIMESTAMP,
    ordered_by BIGINT REFERENCES users(id),
    notes TEXT,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_lab_orders_patient ON lab_orders(patient_id);
CREATE INDEX idx_lab_orders_status ON lab_orders(status);
CREATE INDEX idx_lab_orders_accession ON lab_orders(accession_number);

CREATE TABLE lab_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES lab_orders(id) ON DELETE CASCADE,
    catalog_id BIGINT REFERENCES lab_test_catalog(id),
    result_id BIGINT REFERENCES lab_results(id),
    test_name VARCHAR(150) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ORDERED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lab_order_items_order ON lab_order_items(order_id);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_CATALOG',   'View Lab Catalog',   'Lab Catalog', 'VIEW',   'View the laboratory test catalog'),
('MANAGE_CATALOG', 'Manage Lab Catalog', 'Lab Catalog', 'MANAGE', 'Add and edit catalog tests'),
('VIEW_ORDER',     'View Lab Orders',    'Lab Orders',  'VIEW',   'View laboratory orders'),
('CREATE_ORDER',   'Create Lab Orders',  'Lab Orders',  'CREATE', 'Place a laboratory order'),
('MANAGE_ORDER',   'Manage Lab Orders',  'Lab Orders',  'MANAGE', 'Update order status and dispatch to analyzers');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module IN ('Lab Catalog', 'Lab Orders');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'DOCTOR' AND p.code IN ('VIEW_CATALOG', 'VIEW_ORDER', 'CREATE_ORDER');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'NURSE' AND p.code IN ('VIEW_CATALOG', 'VIEW_ORDER', 'CREATE_ORDER');

-- ---------------------------------------------------------------------
-- Pages (LAB_ORDERS next to LABORATORY, LAB_CATALOG at the end)
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('LAB_ORDERS',  'Lab Orders', 'Laboratory', '/lab-orders',  'list',     NULL, 10),
('LAB_CATALOG', 'Lab Catalog','Laboratory', '/lab-catalog', 'book-open', NULL, 11);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'ADMIN' AND pg.code IN ('LAB_ORDERS', 'LAB_CATALOG')
ON CONFLICT DO NOTHING;

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code IN ('DOCTOR', 'NURSE') AND pg.code IN ('LAB_ORDERS', 'LAB_CATALOG')
ON CONFLICT DO NOTHING;
