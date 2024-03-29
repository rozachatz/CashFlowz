CREATE TABLE accounts (
    account_id UUID PRIMARY KEY,
    owner_name VARCHAR(20),
    balance DECIMAL(19, 4),
    currency VARCHAR(255),
    created_at TIMESTAMP,
    version INT
);

CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY,
    source_account_id UUID,
    target_account_id UUID,
    amount DECIMAL(19, 4),
    currency VARCHAR(255)
);

ALTER TABLE transactions
ADD CONSTRAINT FK_source_account
FOREIGN KEY (source_account_id)
REFERENCES accounts(account_id);

ALTER TABLE transactions
ADD CONSTRAINT FK_target_account
FOREIGN KEY (target_account_id)
REFERENCES accounts(account_id);

CREATE TABLE transaction_requests (
    request_id UUID PRIMARY KEY,
    transaction_request_status VARCHAR(10),
    transaction_id UUID,
    source_account_id UUID,
    target_account_id UUID,
    amount DECIMAL(19, 4),
    infoMessage VARCHAR(255),
    http_status VARCHAR(10)
);

ALTER TABLE transaction_requests
ADD CONSTRAINT FK_transaction
FOREIGN KEY (transaction_id)
REFERENCES transactions(transaction_id);


-- Insert new account 1
INSERT INTO accounts (account_id, owner_name, balance, currency, version)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bca', 'Bill Gates', 1000.00, 'EUR', 1);

-- Insert new account 2
INSERT INTO accounts (account_id, owner_name, balance, currency, version)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe8', 'Elon Musk', 750.50, 'EUR', 1);

-- Insert new account 3s
INSERT INTO accounts (account_id, owner_name, balance, currency, version)
VALUES ('e4c6f84c-8f92-4f2b-90bb-4352e9379bcb', 'Onasis', 100.00, 'USD', 1);

-- Insert t new account 4

INSERT INTO accounts (account_id, owner_name, balance, currency, version)
VALUES ('6a7d71f0-6f12-45a6-91a1-198272a09fe9','Bakogiannis', 75.50, 'CAD', 1);
