CREATE TABLE account (
    account_id UUID PRIMARY KEY,
    owner_name VARCHAR(20),
    balance DECIMAL(19, 4),
    currency VARCHAR(255),
    created_at TIMESTAMP,
    version INT
);

CREATE TABLE transfer (
    transfer_id UUID PRIMARY KEY,
    source_account_id UUID,
    target_account_id UUID,
    amount DECIMAL(19, 4),
    currency VARCHAR(255)
);

ALTER TABLE transfer
ADD CONSTRAINT FK_source_account
FOREIGN KEY (source_account_id)
REFERENCES account(account_id);

ALTER TABLE transfer
ADD CONSTRAINT FK_target_account
FOREIGN KEY (target_account_id)
REFERENCES account(account_id);

CREATE TABLE transfer_request (
    transfer_request_id UUID PRIMARY KEY,
    transfer_request_status VARCHAR(10),
    transfer_id UUID,
    source_account_id UUID,
    target_account_id UUID,
    amount DECIMAL(19, 4),
    info_message VARCHAR(255),
    http_status VARCHAR(10)
);

ALTER TABLE transfer_request
ADD CONSTRAINT FK_transfer
FOREIGN KEY (transfer_id)
REFERENCES transfer(transfer_id);

INSERT INTO account (account_id, owner_name, balance, currency, version)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bca', 'Bill Gates', 1000.00, 'EUR', 1);

INSERT INTO account (account_id, owner_name, balance, currency, version)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe8', 'Elon Musk', 750.50, 'EUR', 1);

INSERT INTO account (account_id, owner_name, balance, currency, version)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bcb', 'Onasis', 100.00, 'USD', 1);

INSERT INTO account (account_id, owner_name, balance, currency, version)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe9','Bakogiannis', 75.50, 'CAD', 1);
