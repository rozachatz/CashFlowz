package com.moneytransfer.exceptions;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
public class ResourceNotFoundException extends MoneyTransferException {

    public ResourceNotFoundException(List<UUID> resourceIds) {
        super("The resources with ids: "+ resourceIds.stream().map(UUID::toString).collect(Collectors.joining(", "))+" were not found..." );
    }

    public ResourceNotFoundException(UUID resourceId) {
        super("The resource with id: "+resourceId+" was not found: " );
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
