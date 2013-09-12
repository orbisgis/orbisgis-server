ALTER TABLE user ADD COLUMN IF NOT EXISTS verification varchar(255);
UPDATE user SET password = '81430ee1e61a4789fc3c52bad9779c0d221a4b16f2f8d9cb449e4e5d78daf57ac77ded63b66922ce3f341672921493c9f1f58df0176f3c09693671193680c4f0' WHERE email = 'admin@admin.com';
UPDATE version SET version ='4';