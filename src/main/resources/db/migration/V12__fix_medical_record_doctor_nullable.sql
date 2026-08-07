-- =====================================================================
-- Fix: medical_records.doctor_id must be optional (entity allows null)
-- =====================================================================

ALTER TABLE medical_records ALTER COLUMN doctor_id DROP NOT NULL;
