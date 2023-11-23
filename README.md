# MoneyTransfer API

## Table of Contents
- [Introduction](#introduction)
- [Acceptance Criteria](#acceptance-criteria)
- [Requests](#requests)
- [Idempotency](#idempotency)
- [API Documentation](#api-documentation)
- [Data Model](#data-model)
- [Architecture](#architecture)
- [Testing](#testing)
- [Docker](#docker)

## Introduction
This project includes a simple (yet non-stop evolving 😌) REST microservice for handling financial transactions 💸, built with SpringBoot.  

*Currency exchange is now supported, using* "https://freecurrencyapi.com/" 😁.

### Acceptance Criteria
- AC 1: Happy path
- AC 2: Insufficient balance
- AC 3: Transfer in the same account
- AC 4: Source/target account does not exist

## Requests
````bash
curl -X POST -H "Content-Type: application/json" -d "{\"sourceAccountId\": \"79360a7e-5249-4822-b3fe-dabfd40b8737\", \"targetAccountId\": \"ef30b8d1-6c5d-4187-b2c4-ab3c640d1b18\", \"amount\": 30.00}" "http://localhost:8080/api/transfer/optimistic"
````
A POST request to the endpoint "http://localhost:8080/api/transfer/optimistic" initiates a transfer between two accounts. Option for pessimistic locking is also available by the endpoint "http://localhost:8080/api/transfer/pessimistic".

Caching is also supported for some GET requests, e.g. "http://localhost:8080/api/transactions/{minAmount}/{maxAmount}".

### Idempotency
This microservice also supports idempotent POST requests via the endpoint: "http://localhost:8080/api/transfer/request/{requestId}".

## API Documentation
Visit "http://localhost:8080/api/swagger-ui/index.html" to explore the endpoints and try-out the app 😉

## Data Model
### Account
The Account entity represents a bank account with the following properties:

| Field     | Description                    |
|-----------|--------------------------------|
| account_id        | Unique identifier of the account |
| balance           | Decimal number representing the account balance |
| currency          | Currency of the account (e.g., "GBP") |
| createdAt         | Date and time when the account was created |

### Transaction
The Transaction entity represents a financial transaction between two accounts and includes the following properties:

| Field            | Description                          |
|------------------|--------------------------------------|
| transaction_id   | Unique identifier of the transaction |
| source_account_id  | ID of the account sending the funds   |
| target_account_id  | ID of the account receiving the funds |
| amount           | Amount being transferred              |
| currency         | Currency of the transaction           |

### TransactionRequest
The TransactionRequest entity provides idempotent behavior for POST transfer requests.

| Field                 | Description                                          |
|-----------------------|------------------------------------------------------|
| transactionRequest_id | Unique identifier of the TransactionRequest          |
| transaction_id        | ID of the successful Transaction                     |
| errorMessage          | Error message                                        |
| requestStatus         | Status of the TransactionRequest                     |
| jsonBody              | String representation of the jsonBody of the request |

## Architecture 🧐
### Controller
Exposes the endpoints of the application, processes the HTTP requests and sends the appropriate response to the client.

### Data Transfer Objects (Dtos)
Container classes, read-only purposes.

### Service
#### TransactionRequestService
Business Logic for executing a request for a financial transaction.

#### TransactionService
Business logic for performing a financial transactions between two accounts.

### Repository
JPA

### Entity
- TransactionRequest
- Transaction
- Account

### Exceptions
- Custom exceptions
- GlobalAPIExceptionHandler returns the appropriate HTTP status for each custom exception

## Testing 🤓
At the moment, integration tests for services are provided. More to come, as the app progresses! 

*Note: Integration tests use H2 embedded db.*

## Docker
The app and (Postgres) db are now dockerized! ❤️ Let the magic happen by executing the following command:
````bash
docker compose up --build
````
*Important note:* The first time you try-out the money transfer microservice, you are advised to execute these commands instead:
````bash
docker compose up db --build
docker compose up app --build
````
and allow db setup to complete before starting the app.



