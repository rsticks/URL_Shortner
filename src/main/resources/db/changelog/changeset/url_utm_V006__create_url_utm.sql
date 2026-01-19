-- Optional UTM metadata per link (stored only if owner enabled "save UTM")
CREATE TABLE url_utm (
    url_hash varchar(6) PRIMARY KEY,
    utm_source varchar(255),
    utm_medium varchar(255),
    utm_campaign varchar(255),
    utm_content varchar(255),
    utm_term varchar(255),
    created_at timestamptz NOT NULL DEFAULT current_timestamp,
    CONSTRAINT fk_url_utm_url FOREIGN KEY (url_hash) REFERENCES url(hash) ON DELETE CASCADE
);


