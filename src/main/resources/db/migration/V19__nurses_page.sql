-- =====================================================================
-- Nurses directory page access
-- =====================================================================

INSERT INTO role_pages (role_id, page_id)
SELECT r.id, pg.id FROM roles r CROSS JOIN pages pg
WHERE r.code = 'NURSE' AND pg.code = 'NURSES'
ON CONFLICT DO NOTHING;
