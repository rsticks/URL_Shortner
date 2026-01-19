-- Click analytics (anonymized)
-- Notes:
-- - We do NOT store raw IP/User-Agent.
-- - visitor_hash_day is a daily-rotated pseudonymous key to count uniques per day.

CREATE TABLE click_event (
    id BIGSERIAL PRIMARY KEY,
    url_hash varchar(6) NOT NULL,
    clicked_at timestamptz NOT NULL DEFAULT current_timestamp,
    visitor_hash_day char(64) NOT NULL,
    referrer_host varchar(255),
    language varchar(16),
    device_type varchar(16),
    os_family varchar(32),
    browser_family varchar(32),
    is_bot boolean NOT NULL DEFAULT false,
    http_status int NOT NULL,
    result varchar(16) NOT NULL,
    CONSTRAINT fk_click_event_url FOREIGN KEY (url_hash) REFERENCES url(hash) ON DELETE CASCADE
);

CREATE INDEX click_event_url_hash_clicked_at_idx ON click_event (url_hash, clicked_at);
CREATE INDEX click_event_clicked_at_idx ON click_event (clicked_at);

-- Daily rollups per link
CREATE TABLE daily_link_stats (
    url_hash varchar(6) NOT NULL,
    day date NOT NULL,
    clicks bigint NOT NULL DEFAULT 0,
    unique_visitors bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (url_hash, day),
    CONSTRAINT fk_daily_link_stats_url FOREIGN KEY (url_hash) REFERENCES url(hash) ON DELETE CASCADE
);

CREATE INDEX daily_link_stats_day_idx ON daily_link_stats (day);

-- Click breakdowns by dimension (no unique breakdowns, only clicks)
CREATE TABLE daily_link_stats_dim (
    url_hash varchar(6) NOT NULL,
    day date NOT NULL,
    dim_type varchar(32) NOT NULL,
    dim_value varchar(255) NOT NULL,
    clicks bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (url_hash, day, dim_type, dim_value),
    CONSTRAINT fk_daily_link_stats_dim_stats FOREIGN KEY (url_hash, day)
        REFERENCES daily_link_stats(url_hash, day) ON DELETE CASCADE
);

CREATE INDEX daily_link_stats_dim_lookup_idx ON daily_link_stats_dim (url_hash, day, dim_type);

-- Helper table to count unique visitors per link per day
CREATE TABLE daily_unique_visitor (
    url_hash varchar(6) NOT NULL,
    day date NOT NULL,
    visitor_hash_day char(64) NOT NULL,
    PRIMARY KEY (url_hash, day, visitor_hash_day),
    CONSTRAINT fk_daily_unique_visitor_stats FOREIGN KEY (url_hash, day)
        REFERENCES daily_link_stats(url_hash, day) ON DELETE CASCADE
);


