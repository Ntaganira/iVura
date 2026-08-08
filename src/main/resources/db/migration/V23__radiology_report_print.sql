-- =====================================================================
-- Radiology: report printing permission
-- =====================================================================

INSERT INTO permissions (code, name, module, action, description) VALUES
('PRINT_RAD_REPORT', 'Print Radiology Reports', 'Radiology', 'PRINT', 'Download / print verified radiology reports as PDF');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.code = 'PRINT_RAD_REPORT'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'DOCTOR' AND p.code = 'PRINT_RAD_REPORT'
ON CONFLICT DO NOTHING;
