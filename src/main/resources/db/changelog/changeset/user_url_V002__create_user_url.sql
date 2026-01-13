CREATE TABLE user_url (
    url_hash varchar(6) PRIMARY KEY,
    user_id bigint NOT NULL,
    created_at timestamptz NOT NULL DEFAULT current_timestamp,
    CONSTRAINT fk_user_url_url FOREIGN KEY (url_hash) REFERENCES url(hash) ON DELETE CASCADE,
    CONSTRAINT fk_user_url_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX user_url_user_id_idx ON user_url (user_id);


