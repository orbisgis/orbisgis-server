ALTER TABLE user ADD COLUMN IF NOT EXISTS admin_wms tinyint DEFAULT 30;
ALTER TABLE user ADD COLUMN IF NOT EXISTS admin_mapcatalog tinyint DEFAULT 30;
ALTER TABLE user ADD COLUMN IF NOT EXISTS admin_wps tinyint DEFAULT 30;
UPDATE version SET version ='2';