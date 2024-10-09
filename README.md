# CashFlowz 💸 💸

## Table of Contents

- [Introduction](#introduction)
- [API Documentation](#API-Documentation)
- [MoneyTransfer Microservice](#moneytransfer-microservice)
- [Notifications Microservice](#notifications-microservice)
- [App Events](#app-events)
- [Docker Guidelines](#docker-guidelines)

## Introduction

CashFlowz is a Java-based microservices application for money transfers. It features an idempotent API and uses event-driven architecture for asynchronous communication between services.

## API Documentation
Power-up the application (preferably with [Docker](#docker)) and
visit "http://localhost:8080/api/swagger-ui/index.html" to explore endpoints and  read the API documentation! 😊

## MoneyTransfer Microservice
### Architecture
The moneytransfer REST microservice architecture follows a domain-driven design pattern.


#### Presentation Layer
Includes the endpoints and their Swagger documentation (MoneyTransferAPIController).
___
#### Application Layer
**Services**:

- GetTransferService: retrieves transfers.

- GetAccountService: retrieves bank accounts.

- MoneyTransferService: performs the money transfer operation.

- CurrencyExchangeService: Gets the exchange rates from third party API (provided by https://freecurrencyapi.com/) using CurrencyExchangeDao. Performs currency exchange for a given rate and amount.

- TransferRequestService: gets, creates and completes transfer requests.

**Aspects**: 

- IdempotentTransferAspect: An @Around aspect providing the functionality for idempotent transfer requests. It publishes a Kafka consumer topic for sending transfer notifications. 
___
#### Domain Layer

The **Account** entity represents a bank account with the following properties:

| Field      | Description                      |      
|------------|----------------------------------|      
| account_id | Unique identifier of the Account |      
| owner_name | Owner name                       |      
| balance    | Account balance                  |     
| currency   | Currency of the Account          |                   
| created_at | Creation date.                   |      

The **Transfer** entity represents a financial transfer between two accounts:

| Field             | Description                           |         
|-------------------|---------------------------------------|         
| transfer_id       | Unique identifier of the Transfer.    |         
| source_account_id | ID of the account sending the funds   |         
| target_account_id | ID of the account receiving the funds |        
| amount            | The transfer amount                   |         
| currency          | Transfer currency                     |         

The **TransferRequest** entity represents a transfer request:

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
___
#### Persistence Layer
Includes all (**JPA**) repositories and Data Access Objects (DAOs).

---
###  Exception Handling & Logging
Using @ControllerAdvice for exception handling and logging.

### Testing
Unit and integration tests with **Junit-5** and **@TestContainers**.
#### Acceptance Criteria

- AC 1: Happy path
- AC 2: Insufficient balance
- AC 3: Transfer in the same account
- AC 4: Source/target account does not exist
___
## Notifications Microservice
The microservice features a Kafka consumer subscribed to the notification topic. This consumer listens for **TransferCompletedEvent** events and sends a transfer notification (log) message.

The **TransferNotificationsService** includes the logic for sending a transfer notification.


### Testing
Unit and integration tests with **Junit-5**.
____
## App Events
The money transfer and notifications microservices communicate asynchronously with events (**TransferCompletedEvent**) using **Kafka** with **Redpanda**.
___
## Docker Guidelines

Build the project using Maven and let the magic ✨ happen by executing:

````bash
docker compose up --build
````

That's all you need, everything is set up for you :)

You can of course play with whatever database/cache/migration tool you like (I use Postgres, Redis and Flyway) by
modifying the docker-compose.yml file.

Have fun! 
