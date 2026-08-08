-- =====================================================================
-- Digital consent signature for patients
-- =====================================================================

ALTER TABLE patients ADD COLUMN signature_data TEXT;
ALTER TABLE patients ADD COLUMN consent_date TIMESTAMP;
ALTER TABLE patients ADD COLUMN consent_given BOOLEAN NOT NULL DEFAULT FALSE;
