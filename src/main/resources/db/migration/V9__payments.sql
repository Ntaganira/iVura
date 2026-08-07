-- =====================================================================
-- Payments / Collections module (module: Payments)
-- =====================================================================

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    billing_id BIGINT NOT NULL REFERENCES billings(id),
    amount_paid NUMERIC(10, 2) NOT NULL,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    recorded_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_payments_billing_id ON payments(billing_id);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_PAYMENT',   'View Payments',   'Payment Management', 'VIEW',   'View the payment list'),
('CREATE_PAYMENT', 'Create Payment',  'Payment Management', 'CREATE', 'Record a new payment'),
('EDIT_PAYMENT',   'Edit Payment',    'Payment Management', 'EDIT',   'Update a payment'),
('DELETE_PAYMENT', 'Delete Payment',  'Payment Management', 'DELETE', 'Delete a payment');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module = 'Payment Management';

-- ---------------------------------------------------------------------
-- Page (child of Billing)
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('PAYMENTS', 'Payments', 'Payments', '/payments', 'credit-card',
 (SELECT id FROM pages WHERE code = 'BILLING'), 13);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'ADMIN' AND pg.code = 'PAYMENTS';
