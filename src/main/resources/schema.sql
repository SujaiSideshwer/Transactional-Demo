CREATE TABLE IF NOT EXISTS account(
    id          BIGINT          PRIMARY KEY,
    owner       VARCHAR(255)    NOT NULL,
    balance     DECIMAL(19,2)   NOT NULL
);