# CashFlowz 💸 💸

## Table of Contents

- [Introduction](#introduction)
- [API Documentation](#API-Documentation)
- [Architecture](#architecture)
- [Testing](#testing)
- [Kafka](#kafka)
- [Docker](#docker)

## Introduction

CashFlowz is a money transfer microservice application written in Java for seamless and secure financial transfers 💸 .

## API Documentation

Power-up the application (preferably with [Docker](#docker)) and
visit "http://localhost:8080/api/swagger-ui/index.html" to explore endpoints and  read the API documentation! 😊

## Architecture
### MoneyTransfer Microservice
The moneytransfer microservice architecture follows a domain-driven design pattern.

### Presentation Layer

All endpoints and their documentation are defined in MoneyTransferAPIController.

### Application Layer
Includes all the services and DTOs.

- #### GetTransferService

Gets all transfers within the system.

- #### GetAccountService

Gets all accounts within the system.

- #### MoneyTransferService

Performs the money transfer operation.

- #### CurrencyExchangeService

Performs currency exchange by retrieving the latest exchange rates from "https://freecurrencyapi.com/"! 💱

- #### TransferRequestService

Gets, submits and resolves all transfer requests.

- #### CurrencyExchangeService
Gets the exchange rates from third party API using CurrencyExchangeDao and performs the currency exchange for a given amount.

### Domain Layer

#### Account

The Account entity represents a bank account with the following properties:

| Field      | Description                      |      
|------------|----------------------------------|      
| account_id | Unique identifier of the Account |      
| owner_name | Owner name                       |      
| balance    | Account balance                  |     
| currency   | Currency of the Account          |                   
| created_at | Creation date.                   |      

#### Transfer

The Transfer entity represents a financial transfer between two accounts:

| Field             | Description                           |         
|-------------------|---------------------------------------|         
| transfer_id       | Unique identifier of the Transfer.    |         
| source_account_id | ID of the account sending the funds   |         
| target_account_id | ID of the account receiving the funds |        
| amount            | The transfer amount                   |         
| currency          | Transfer currency                     |         

#### TransferRequest

The TransferRequest entity represents a transfer request:

| Field                   | Description                                            | 
|-------------------------|--------------------------------------------------------| 
| transfer_request_id     | Unique identifier of the TransferRequest               | 
| source_account_id       | ID of the account sending the funds                    | 
| target_account_id       | ID of the account receiving the funds                  | 
| amount                  | The transfer amount                                    | 
| transfer_request_status | The status of a TransferRequest                        |
| transfer                | the associated Transfer of a completed TransferRequest | 
| http_status             | http status of a completed TransferRequest             | 
| info_message            | includes the exception message or a success message    | 

### Notifications Microservice
The Notifications microservice sends a notification to the customer when a Transfer is successfully completed.

### Kafka
The money transfer and notifications microservices communicate asynchronously with Kafka.

### Persistence Layer
Includes all (JPA) repositories and Data Access Objects (DAOs).
_______________________________________

## Aspect Oriented Programming

### IdempotentTransferAspect

An @Around aspect providing the functionality for idempotent transfer requests.
______________________________

## Logging & Exception Handling

Using @ControllerAdvice for exception handling and logging.

## Testing
Unit and integration tests with Junit-5 and @TestContainers.

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
