package com.github.petha.correlationengine.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }
}
