-- =====================================================================
-- Doctor Attendance & Shift Scheduling module
-- =====================================================================

CREATE TABLE doctor_shifts (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    day_of_week SMALLINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    shift_label VARCHAR(50),
    CONSTRAINT uk_doctor_day UNIQUE (doctor_id, day_of_week)
);

CREATE INDEX idx_doctor_shifts_doctor ON doctor_shifts(doctor_id);

CREATE TABLE attendances (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    attendance_date DATE NOT NULL,
    clock_in TIME,
    clock_out TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_doctor_date UNIQUE (doctor_id, attendance_date)
);

CREATE INDEX idx_attendances_date ON attendances(attendance_date);

-- ---------------------------------------------------------------------
-- Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, name, module, action, description) VALUES
('VIEW_ATTENDANCE',   'View Attendance',       'Attendance', 'VIEW',   'View doctor attendance'),
('CREATE_ATTENDANCE', 'Record Attendance',     'Attendance', 'CREATE', 'Check a doctor in or out'),
('EDIT_ATTENDANCE',   'Edit Attendance',       'Attendance', 'EDIT',   'Update an attendance record'),
('MANAGE_SHIFT',      'Manage Shift Schedule', 'Attendance', 'MANAGE', 'Set doctor shift schedules');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.module = 'Attendance';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'DOCTOR' AND p.code IN ('VIEW_ATTENDANCE', 'CREATE_ATTENDANCE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'NURSE' AND p.code IN ('VIEW_ATTENDANCE');

-- ---------------------------------------------------------------------
-- Page
-- ---------------------------------------------------------------------
INSERT INTO pages (code, name, module, path, icon, parent_id, sort_order) VALUES
('ATTENDANCE', 'Attendance', 'Attendance', '/attendance', 'clock', NULL, 6);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'ADMIN' AND pg.code = 'ATTENDANCE';

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code IN ('DOCTOR', 'NURSE') AND pg.code = 'ATTENDANCE'
ON CONFLICT DO NOTHING;
