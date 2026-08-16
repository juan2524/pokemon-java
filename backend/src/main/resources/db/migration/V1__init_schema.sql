-- US03/US04: users, local pokemon records, tags

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(16)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT users_role_chk CHECK (role IN ('USER', 'ADMIN'))
);

CREATE UNIQUE INDEX ux_users_email ON users (email);

CREATE TABLE pokemon_record (
    id               UUID PRIMARY KEY,
    poke_api_id      INTEGER      NOT NULL,
    name             VARCHAR(100) NOT NULL,
    localized_name   VARCHAR(100),
    region           VARCHAR(100),
    internal_notes   VARCHAR(2000),
    synced_at        TIMESTAMPTZ  NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX ux_pokemon_record_poke_api_id ON pokemon_record (poke_api_id);
CREATE INDEX ix_pokemon_record_name ON pokemon_record (name);

CREATE TABLE pokemon_tag (
    id         UUID PRIMARY KEY,
    pokemon_id UUID         NOT NULL REFERENCES pokemon_record (id) ON DELETE CASCADE,
    tag        VARCHAR(64)  NOT NULL,
    CONSTRAINT pokemon_tag_unique UNIQUE (pokemon_id, tag)
);

CREATE INDEX ix_pokemon_tag_pokemon_id ON pokemon_tag (pokemon_id);
CREATE INDEX ix_pokemon_tag_tag ON pokemon_tag (tag);
