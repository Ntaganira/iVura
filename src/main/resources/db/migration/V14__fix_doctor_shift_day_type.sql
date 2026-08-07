-- =====================================================================
-- Fix: doctor_shifts.day_of_week must be INTEGER to match the entity
-- =====================================================================

ALTER TABLE doctor_shifts ALTER COLUMN day_of_week TYPE INTEGER;
