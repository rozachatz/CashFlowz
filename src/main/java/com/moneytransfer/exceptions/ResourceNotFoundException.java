package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResourceNotFoundException extends MoneyTransferException {

    public ResourceNotFoundException(Class<?> resourceClass, UUID... resourceIds) {

        super(String.format("$1%s: [$2%s] not found.", resourceClass.getName(),
                Arrays.stream(resourceIds).map(UUID::toString).collect(Collectors.joining(", "))));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
