-- =====================================================================
-- Activity Log (Audit Trail)
-- Tracks user activities: CRUD operations, logins, logouts, access denied
-- =====================================================================

CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(50),
    module VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activity_logs_user_id ON activity_logs (user_id, created_at DESC);
CREATE INDEX idx_activity_logs_created_at ON activity_logs (created_at);
CREATE INDEX idx_activity_logs_module ON activity_logs (module);
CREATE INDEX idx_activity_logs_action ON activity_logs (action);

-- ---------------------------------------------------------------------
-- Permission: admins can view all activity logs
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_ACTIVITY_LOG', 'View Activity Logs', 'Activity Logs', 'VIEW', 'View activity logs of all users');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.code = 'VIEW_ACTIVITY_LOG';

-- ---------------------------------------------------------------------
-- Pages: "My Activity" for every user, "Activity Logs" for administrators
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('MY_ACTIVITY',   'My Activity',   'My Activity',   '/activity/me', 'clock',     NULL, 14),
('ACTIVITY_LOGS', 'Activity Logs', 'Activity Logs', '/activity',    'file-text', 19,    4);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, p.id FROM roles r CROSS JOIN pages p WHERE p.code = 'MY_ACTIVITY';

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, p.id FROM roles r CROSS JOIN pages p
WHERE r.code = 'ADMIN' AND p.code = 'ACTIVITY_LOGS';
