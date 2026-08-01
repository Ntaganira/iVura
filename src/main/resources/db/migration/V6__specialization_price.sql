-- =====================================================================
-- Specialization price: a specialization is what a doctor provides
-- to patients, with an attached consultation/service price
-- =====================================================================

ALTER TABLE specializations ADD COLUMN price DECIMAL(10,2) NOT NULL DEFAULT 0;

UPDATE specializations SET price = 20000 WHERE name = 'Cardiology';
UPDATE specializations SET price = 15000 WHERE name = 'Pediatrics';
UPDATE specializations SET price = 25000 WHERE name = 'Orthopedics';
UPDATE specializations SET price = 30000 WHERE name = 'Neurology';
UPDATE specializations SET price = 12000 WHERE name = 'General Medicine';
UPDATE specializations SET price = 20000 WHERE name = 'Emergency';
