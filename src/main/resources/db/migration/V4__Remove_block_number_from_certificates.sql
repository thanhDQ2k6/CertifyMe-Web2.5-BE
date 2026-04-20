-- ==============================================================
-- Version: 4.0
-- Remove redundant block_number column from certificates table
-- ==============================================================
ALTER TABLE certificates
DROP COLUMN block_number;