-- Add email field to users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email varchar(255);

-- Backfill existing rows (username is unique so derived email is unique too)
UPDATE users
SET email = username || '@example.com'
WHERE email IS NULL;

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

-- Ensure uniqueness
CREATE UNIQUE INDEX IF NOT EXISTS users_email_uidx ON users (email);


