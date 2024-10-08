package com.cashflowz.moneytransfer.controller;

import com.cashflowz.moneytransfer.exceptions.MoneyTransferException;
import com.cashflowz.moneytransfer.exceptions.ResourceNotFoundException;
import com.cashflowz.moneytransfer.dto.GetAccountDto;
import com.cashflowz.moneytransfer.dto.GetTransferDto;
import com.cashflowz.moneytransfer.dto.NewTransferDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * Controller handling money transfer requests
 * and retrieval of account/transfer resources.
 */
public interface MoneyTransferAPIController {
    @Operation(summary = "Gets transfer by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer with the given id was found!", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GetTransferDto.class))}),
            @ApiResponse(responseCode = "404", description = "No transfer with the given id was found!", content = @Content)})
    ResponseEntity<GetTransferDto> getTransferById(
            @Parameter(description = "The transfer id.", required = true) UUID transferId)
            throws ResourceNotFoundException;

    @Operation(summary = "Gets account by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "An account with the given id was found!", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GetTransferDto.class))}),
            @ApiResponse(responseCode = "404", description = "No account was found!", content = @Content)})
    ResponseEntity<GetAccountDto> getAccountById(
            @Parameter(description = "The account id.", required = true) UUID accountId)
            throws ResourceNotFoundException;

    @Operation(summary = "Performs an idempotent transfer request. The currency of the transfer is identical to the source's.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer request completed successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GetTransferDto.class))}),
            @ApiResponse(responseCode = "404", description = "The specified source or target account was not found.", content = @Content),
            @ApiResponse(responseCode = "402", description = "Insufficient balance for executing the money transfer.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Transfers within the same account are not allowed.", content = @Content),
            @ApiResponse(responseCode = "409", description = "This transfer request has already been completed but there is a conflict with the provided json body.", content = @Content)

    })
    ResponseEntity<GetTransferDto> transferRequest(
            @Parameter(description = "The transferRequestId, the source, target accounts and the amount to be transferred.", required = true) NewTransferDto newTransferDTO)
            throws MoneyTransferException;

    @Operation(summary = "Fetches accounts with a maximum number of records parameter.")
    ResponseEntity<List<GetAccountDto>> getAccounts(@Parameter(description = "The page maxRecords.", required = true) int maxRecords);

    @Operation(summary = "Fetches accounts with a maximum number of records parameter.")
    ResponseEntity<List<GetTransferDto>> getTransfers(
            int maxRecords) throws ResourceNotFoundException;

}