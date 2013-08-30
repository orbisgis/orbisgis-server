ALTER TABLE owscontext ADD COLUMN IF NOT EXISTS description varchar(1000);
UPDATE version SET version ='6';