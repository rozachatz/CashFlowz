# MoneyTransfer API 💸 💸 

## Table of Contents
- [Introduction](#introduction)
- [Documentation](#documentation)
- [Data Model](#data-model)
- [Architecture](#architecture)
- [Testing](#testing)
- [Docker Guidelines](#docker-guidelines)

## Introduction 
This project includes a SpringBoot application for handling financial transactions 💸 .  

## Documentation
Powered by Swagger. Power-up the application (preferably with [Docker](#docker)) and visit "http://localhost:8080/api/swagger-ui/index.html" to explore endpoints, read API documentation and try-out the app! 😊

## Data Model
### Account
The Account entity represents a bank account with the following properties:

| Field      | Description                                    |
|------------|------------------------------------------------|
| account_id | Unique identifier of the account               |
| owner_name | Name of the account owner                      |
| balance    | Decimal number representing the account balance |
| currency   | Currency of the account           |
| created_at | Date and time of account creation.             |

### Transaction
The Transaction entity represents a financial transaction between two accounts and includes the following properties:

| Field             | Description                          |
|-------------------|--------------------------------------|
| id                | Unique identifier of the transaction |
| source_account_id | ID of the account sending the funds  |
| target_account_id | ID of the account receiving the funds |
| amount            | Amount being transferred             |
| currency          | currency of the transaction          |

### Request
The Request entity represents an idempotent money transfer transactionRequest:

| Field             | Description                                  |
|-------------------|----------------------------------------------|
| id                | Unique identifier of the transactionRequest  |
| source_account_id | ID of the account sending the funds          |
| target_account_id | ID of the account receiving the funds        |
| amount            | Amount of funds being transferred            |
| transaction       | the associated Transaction                   |
| http_status       | http status of the associated post request.  |
| info_message      | detailed information for the request outcome |

## Architecture
### Controller
MoneyTransferAPIController

### Data Transfer Objects
Records, ready-only.

### Aspects
#### IdempotentTransferAspect 
The aspect that provides the functionality for an idempotent transactionRequests.

### Services
#### GetTransactionService
Gets all transactions within the system.

#### GetAccountService
Gets all accounts within the system.

#### MoneyTransferService
Performs the money transfer operation.

#### RequestService
Gets, submits and resolves all transaction requests using cache mechanisms.

#### CurrencyExchangeService
Performs currency exchange from the source currency to the target currency by retrieving the latest exchange rates from "https://freecurrencyapi.com/"! 💱


### Entities
- Transaction
- Account
- TransactionRequest

### Repositories
JPA repositories.

### Exceptions
@ControllerAdvice for handling all custom exceptions of the application.
  
## Testing
At the moment, service integration tests using H2 embedded db are provided.

### Acceptance Criteria
- AC 1: Happy path
- AC 2: Insufficient balance
- AC 3: Transfer in the same account
- AC 4: Source/target account does not exist
  
## Docker Guidelines
The application and database are now dockerized! Let the magic ✨ happen by following the instructions.

First package the application into a JAR by executing:
````bash
mvn clean package
````
Now you can power up the application *anytime* by just executing:
````bash
docker compose up --build
````

