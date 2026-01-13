CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    role varchar(32) NOT NULL DEFAULT 'USER',
    subscription_expires_at timestamptz,
    enabled boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT current_timestamp
);

CREATE INDEX users_username_idx ON users (username);


