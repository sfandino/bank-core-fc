-- V1__init.sql - Initial DDL for Users, Transactions and Curerncies Tables
CREATE TABLE users (
    id          UUID        PRIMARY KEY,
    name        TEXT        NOT NULL,
    email       TEXT        UNIQUE NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE currencies (
    code        CHAR(3)     PRIMARY KEY,       
    name        TEXT        NOT NULL,          
    symbol      TEXT,                         
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE transactions (
    id              UUID          PRIMARY KEY,
    sender_id       UUID          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    receiver_id     UUID          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    currency_code   CHAR(3)       NOT NULL REFERENCES currencies(code) ON UPDATE CASCADE ON DELETE RESTRICT,
    amount          NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    status          VARCHAR(10)   NOT NULL CHECK (status IN ('pending','completed','failed')),
    occurred_at     TIMESTAMPTZ   NOT NULL, -- when the payment event happened
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(), 
    UNIQUE (sender_id, receiver_id, occurred_at, amount) -- to help catch dupes
);

-- Indexes for performance optimization - more as an example, this may vary based on actual usage patterns and ingestion etc...
CREATE INDEX idx_tx_sender_ts       ON transactions(sender_id, occurred_at);
CREATE INDEX idx_tx_receiver_ts     ON transactions(receiver_id, occurred_at);
CREATE INDEX idx_tx_currency        ON transactions(currency_code);