# CashFlowz 💸 💸

## Table of Contents

- [Introduction](#introduction)
- [API Documentation](#API-Documentation)
- [Architecture](#architecture)
- [Testing](#testing)
- [Docker](#docker)

## Introduction

CashFlowz is a Java application for seamless and secure financial transactions 💸 .

## API Documentation

Power-up the application (preferably with [Docker](#docker-guidelines)) and
visit "http://localhost:8080/api/swagger-ui/index.html" to explore endpoints, read API documentation and try-out the
app! 😊

## Architecture

The app follows a three-tier layered architecture, consisting of the Presentation Layer (Controller(s)), Business
Layer (Services) and Persistent Layer (Repositories/Entities).

### Presentation Layer

All endpoints and their corresponding swagger documentation are defined in the MoneyTransferAPIController.

### Business Layer

- #### GetTransactionService

Gets all transactions within the system.

- #### GetAccountService

Gets all accounts within the system.

- #### MoneyTransferService

Performs the money transfer operation.

- #### CurrencyExchangeService

Performs currency exchange by retrieving the latest exchange rates from "https://freecurrencyapi.com/"! 💱

- #### RequestService

Gets, submits and resolves all transaction requests, which are stored in a Redis cache (i.e., requestsCache).

### Persistent Layer

Using JPA repositories for each entity:

#### Account

The Account entity represents a bank account with the following prope

| Field      | Description                                     |      
|------------|-------------------------------------------------|      
| account_id | Unique identifier of the account                |      
| owner_name | Name of the account owner                       |      
| balance    | Decimal number representing the account balance |     
| currency   | Currency of the account                         |                   
| created_at | Date and time of account creation.              |      

#### Transaction

The Transaction entity represents a financial transaction between two

| Field             | Description                           |         
|-------------------|---------------------------------------|         
| transaction_id    | Unique identifier of the transaction. |         
| source_account_id | ID of the account sending the funds   |         
| target_account_id | ID of the account receiving the funds |        
| amount            | Amount being transferred              |         
| currency          | currency of the transaction           |         

#### TransactionRequest

The TransactionRequest entity represents an idempotent transfer reque

| Field             | Description                                  | 
|-------------------|----------------------------------------------| 
| request_id        | Unique identifier of the transactionRequest  | 
| source_account_id | ID of the account sending the funds          | 
| target_account_id | ID of the account receiving the funds        | 
| amount            | Amount of funds being transferred            | 
| transaction       | the associated Transaction                   | 
| http_status       | http status of the associated post request.  | 
| info_message      | detailed information for the request outcome | 

_______________________________________

### Aspect Oriented Programming

- ##### IdempotentTransferAspect

Provides the functionality for an idempotent transfer request.
______________________________

### Logging & Exception Handling

Using @ControllerAdvice for exception handling and logging.

## Testing

At the moment, service integration tests (with H2 test database) are provided.

### Acceptance Criteria

- AC 1: Happy path
- AC 2: Insufficient balance
- AC 3: Transfer in the same account
- AC 4: Source/target account does not exist

## Docker

Build the project and let the magic ✨ happen by executing:

````bash
docker compose up --build
````

That's all you need, everything is set up for you :)

You can of course play with whatever database/cache/migration tool you like (I use Postgres, Redis and Flyway) by
modifying the docker-compose.yml file.

Have fun! 
