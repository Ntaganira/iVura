-- =====================================================================
-- User Management Module: roles, permissions, pages (module access)
-- =====================================================================

ALTER TABLE roles
    ADD COLUMN code VARCHAR(50),
    ADD COLUMN description TEXT,
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP;

UPDATE roles SET code = UPPER(REPLACE(name, 'ROLE_', ''));

ALTER TABLE roles ALTER COLUMN code SET NOT NULL;
ALTER TABLE roles ADD CONSTRAINT uk_roles_code UNIQUE (code);

-- ---------------------------------------------------------------------
-- Permissions (action-level, organized by module)
-- ---------------------------------------------------------------------
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    module VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Pages / modules a role can access (hierarchical: parent_id)
-- ---------------------------------------------------------------------
CREATE TABLE pages (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    module VARCHAR(100) NOT NULL,
    path VARCHAR(255) NOT NULL,
    icon VARCHAR(50),
    parent_id BIGINT REFERENCES pages(id),
    sort_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE role_pages (
    role_id BIGINT NOT NULL REFERENCES roles(id),
    page_id BIGINT NOT NULL REFERENCES pages(id),
    PRIMARY KEY (role_id, page_id)
);

-- ---------------------------------------------------------------------
-- Seed permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (id, code, name, module, action, description) VALUES
(1,  'VIEW_PATIENT',       'View Patients',       'Patient Management',    'VIEW',    'View the patient list and patient details'),
(2,  'CREATE_PATIENT',     'Create Patient',      'Patient Management',    'CREATE',  'Register a new patient'),
(3,  'EDIT_PATIENT',       'Edit Patient',        'Patient Management',    'EDIT',    'Update patient information'),
(4,  'DELETE_PATIENT',     'Delete Patient',      'Patient Management',    'DELETE',  'Deactivate or delete a patient'),

(5,  'VIEW_APPOINTMENT',   'View Appointments',   'Appointment Management','VIEW',    'View the appointment list and details'),
(6,  'CREATE_APPOINTMENT', 'Create Appointment',  'Appointment Management','CREATE',  'Schedule a new appointment'),
(7,  'EDIT_APPOINTMENT',   'Edit Appointment',    'Appointment Management','EDIT',    'Update appointment details'),
(8,  'CANCEL_APPOINTMENT', 'Cancel Appointment',  'Appointment Management','CANCEL',  'Cancel or reschedule an appointment'),

(9,  'VIEW_DOCTOR',        'View Doctors',        'Doctor Management',     'VIEW',    'View the doctor list and details'),
(10, 'CREATE_DOCTOR',      'Create Doctor',       'Doctor Management',     'CREATE',  'Register a new doctor'),
(11, 'EDIT_DOCTOR',        'Edit Doctor',         'Doctor Management',     'EDIT',    'Update doctor information'),
(12, 'DELETE_DOCTOR',      'Delete Doctor',       'Doctor Management',     'DELETE',  'Deactivate or delete a doctor'),

(13, 'VIEW_BILL',          'View Bills',          'Billing',               'VIEW',    'View bills and invoices'),
(14, 'CREATE_BILL',        'Create Bill',         'Billing',               'CREATE',  'Create a new bill'),
(15, 'EDIT_BILL',          'Edit Bill',           'Billing',               'EDIT',    'Update bill details'),
(16, 'DELETE_BILL',        'Delete Bill',         'Billing',               'DELETE',  'Delete a bill'),
(17, 'APPROVE_PAYMENT',    'Approve Payment',     'Billing',               'APPROVE', 'Approve pending payments'),

(18, 'VIEW_DASHBOARD',     'View Dashboard',      'Dashboard',             'VIEW',    'View the dashboard'),

(19, 'VIEW_USER',          'View Users',          'User Management',       'VIEW',    'View system users'),
(20, 'CREATE_USER',        'Create User',         'User Management',       'CREATE',  'Create a new system user'),
(21, 'EDIT_USER',          'Edit User',           'User Management',       'EDIT',    'Edit a system user'),
(22, 'DELETE_USER',        'Delete User',         'User Management',       'DELETE',  'Delete or deactivate a user'),
(23, 'RESET_PASSWORD',     'Reset Password',      'User Management',       'RESET',   'Reset a user password'),
(24, 'ASSIGN_ROLE',        'Assign Roles',        'User Management',       'ASSIGN',  'Assign roles to a user'),

(25, 'VIEW_ROLE',          'View Roles',          'Role Management',       'VIEW',    'View system roles'),
(26, 'CREATE_ROLE',        'Create Role',         'Role Management',       'CREATE',  'Create a new role'),
(27, 'EDIT_ROLE',          'Edit Role',           'Role Management',       'EDIT',    'Edit a role'),
(28, 'DELETE_ROLE',        'Delete Role',         'Role Management',       'DELETE',  'Delete a role'),

(29, 'VIEW_PERMISSION',    'View Permissions',    'Permission Management', 'VIEW',    'View permissions'),
(30, 'CREATE_PERMISSION',  'Create Permission',   'Permission Management', 'CREATE',  'Create a new permission'),
(31, 'EDIT_PERMISSION',    'Edit Permission',     'Permission Management', 'EDIT',    'Edit a permission'),
(32, 'DELETE_PERMISSION',  'Delete Permission',   'Permission Management', 'DELETE',  'Delete a permission'),

(33, 'VIEW_REPORT',        'View Reports',        'Reports',               'VIEW',    'View reports'),
(34, 'EXPORT_REPORT',      'Export Reports',      'Reports',               'EXPORT',  'Export reports'),
(35, 'PRINT_REPORT',       'Print Reports',       'Reports',               'PRINT',   'Print reports');

SELECT setval('permissions_id_seq', (SELECT MAX(id) FROM permissions));

-- ---------------------------------------------------------------------
-- Seed pages (modules and sub-pages)
-- ---------------------------------------------------------------------
INSERT INTO pages (id, code, name, module, path, icon, parent_id, sort_order) VALUES
(1,  'DASHBOARD',           'Dashboard',             'Dashboard',             '/dashboard',                'grid',     NULL,  1),
(2,  'PATIENTS',            'Patient Management',    'Patients',              '/patients',                 'users',    NULL,  2),
(3,  'PATIENT_LIST',        'Patient List',          'Patients',              '/patients',                 'list',     2,     1),
(4,  'PATIENT_REGISTER',    'Register Patient',      'Patients',              '/patients/add',             'plus',     2,     2),
(5,  'PATIENT_PROFILE',     'Patient Profile',       'Patients',              '/patients/{id}',            'user',     2,     3),
(6,  'PATIENT_HISTORY',     'Medical History',       'Patients',              '/patients/{id}/history',    'file-text', 2,    4),
(7,  'DOCTORS',             'Doctors',               'Doctors',               '/doctors',                  'users',    NULL,  3),
(8,  'NURSES',              'Nurses',                'Nurses',                '/nurses',                   'users',    NULL,  4),
(9,  'APPOINTMENTS',        'Appointment Management','Appointments',          '/appointments',             'calendar', NULL,  5),
(10, 'APPOINTMENT_LIST',    'Appointment List',      'Appointments',          '/appointments',             'list',     9,     1),
(11, 'APPOINTMENT_CREATE',  'Create Appointment',    'Appointments',          '/appointments/add',         'plus',     9,     2),
(12, 'APPOINTMENT_CALENDAR','Appointment Calendar',  'Appointments',          '/appointments/calendar',    'calendar', 9,     3),
(13, 'ADMISSIONS',          'Admissions',            'Admissions',            '/admissions',               'home',     NULL,  6),
(14, 'PHARMACY',            'Pharmacy',              'Pharmacy',              '/pharmacy',                 'pill',     NULL,  7),
(15, 'LABORATORY',          'Laboratory',            'Laboratory',            '/laboratory',               'beaker',   NULL,  8),
(16, 'BILLING',             'Billing',               'Billing',               '/billings',                 'dollar',   NULL,  9),
(17, 'INSURANCE',           'Insurance',             'Insurance',             '/insurance',                'shield',   NULL, 10),
(18, 'REPORTS',             'Reports',               'Reports',               '/reports',                  'bar-chart', NULL, 11),
(19, 'USER_MANAGEMENT',     'User Management',       'User Management',       '/users',                    'settings', NULL, 12),
(20, 'USERS',               'Users',                 'User Management',       '/users',                    'users',    19,    1),
(21, 'ROLES',               'Roles',                 'User Management',       '/roles',                    'shield',   19,    2),
(22, 'PERMISSIONS',         'Permissions',           'User Management',       '/permissions',              'lock',     19,    3),
(23, 'SYSTEM_SETTINGS',     'System Settings',       'System Settings',       '/settings',                 'settings', NULL, 13);

SELECT setval('pages_id_seq', (SELECT MAX(id) FROM pages));

-- ---------------------------------------------------------------------
-- Seed role -> permission assignment
-- ---------------------------------------------------------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.code = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'DOCTOR' AND p.code IN (
    'VIEW_PATIENT','CREATE_PATIENT','EDIT_PATIENT',
    'VIEW_APPOINTMENT','CREATE_APPOINTMENT','EDIT_APPOINTMENT',
    'VIEW_DOCTOR','CREATE_DOCTOR','EDIT_DOCTOR',
    'VIEW_DASHBOARD','VIEW_REPORT'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'NURSE' AND p.code IN (
    'VIEW_PATIENT','CREATE_PATIENT','EDIT_PATIENT',
    'VIEW_APPOINTMENT','CREATE_APPOINTMENT','EDIT_APPOINTMENT','CANCEL_APPOINTMENT',
    'VIEW_DOCTOR',
    'VIEW_DASHBOARD','VIEW_REPORT'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'STAFF' AND p.code IN (
    'VIEW_PATIENT','VIEW_DOCTOR','VIEW_APPOINTMENT','VIEW_DASHBOARD'
);

-- ---------------------------------------------------------------------
-- Seed role -> page assignment
-- ---------------------------------------------------------------------
INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg WHERE r.code = 'ADMIN';

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'DOCTOR' AND pg.code IN (
    'DASHBOARD',
    'PATIENTS','PATIENT_LIST','PATIENT_REGISTER','PATIENT_PROFILE','PATIENT_HISTORY',
    'DOCTORS',
    'APPOINTMENTS','APPOINTMENT_LIST','APPOINTMENT_CREATE','APPOINTMENT_CALENDAR'
);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'NURSE' AND pg.code IN (
    'DASHBOARD',
    'PATIENTS','PATIENT_LIST','PATIENT_REGISTER','PATIENT_PROFILE','PATIENT_HISTORY',
    'DOCTORS',
    'APPOINTMENTS','APPOINTMENT_LIST','APPOINTMENT_CREATE','APPOINTMENT_CALENDAR'
);

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'STAFF' AND pg.code IN ('DASHBOARD');
