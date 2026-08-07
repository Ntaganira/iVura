-- =====================================================================
-- Notifications module (per-user in-app notifications)
-- =====================================================================

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    type VARCHAR(20) NOT NULL DEFAULT 'INFO',
    reference_type VARCHAR(50),
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_NOTIFICATION',      'View Notifications',       'Notifications', 'VIEW',   'View own notifications'),
('MARK_NOTIFICATION_READ', 'Mark Notifications Read',  'Notifications', 'EDIT',   'Mark notifications as read'),
('CREATE_NOTIFICATION',    'Send Notification',        'Notifications', 'CREATE', 'Broadcast a notification to all users');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE p.module = 'Notifications' AND r.code IN ('ADMIN', 'DOCTOR', 'NURSE', 'STAFF')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.code = 'CREATE_NOTIFICATION'
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- Page (all roles receive notifications)
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('NOTIFICATIONS', 'Notifications', 'Notifications', '/notifications', 'bell', NULL, 15);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, p.id FROM roles r CROSS JOIN pages p
WHERE p.code = 'NOTIFICATIONS'
ON CONFLICT DO NOTHING;
