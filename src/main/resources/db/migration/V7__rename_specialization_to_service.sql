-- =====================================================================
-- Rename "Specialization" -> "Service" (services provided by the hospital)
-- =====================================================================

-- Table + column
ALTER TABLE specializations RENAME TO services;
ALTER TABLE services RENAME CONSTRAINT specializations_pkey TO services_pkey;
ALTER TABLE services RENAME CONSTRAINT specializations_name_key TO services_name_key;
ALTER TABLE doctors RENAME COLUMN specialization_id TO service_id;
ALTER TABLE doctors RENAME CONSTRAINT doctors_specialization_id_fkey TO doctors_service_id_fkey;
ALTER SEQUENCE specializations_id_seq RENAME TO services_id_seq;

-- Permissions
UPDATE permissions SET code = 'VIEW_SERVICE',   name = 'View Services',   module = 'Service Management' WHERE code = 'VIEW_SPECIALIZATION';
UPDATE permissions SET code = 'CREATE_SERVICE', name = 'Create Service',  module = 'Service Management' WHERE code = 'CREATE_SPECIALIZATION';
UPDATE permissions SET code = 'EDIT_SERVICE',   name = 'Edit Service',    module = 'Service Management' WHERE code = 'EDIT_SPECIALIZATION';
UPDATE permissions SET code = 'DELETE_SERVICE', name = 'Delete Service',  module = 'Service Management' WHERE code = 'DELETE_SPECIALIZATION';

-- Page
UPDATE pages SET code = 'SERVICES', name = 'Services', module = 'Services', path = '/services' WHERE code = 'SPECIALIZATIONS';
