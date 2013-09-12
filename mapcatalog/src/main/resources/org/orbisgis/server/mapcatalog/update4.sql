ALTER TABLE user ADD COLUMN IF NOT EXISTS reset_pass varchar(255);
UPDATE version SET version ='5';