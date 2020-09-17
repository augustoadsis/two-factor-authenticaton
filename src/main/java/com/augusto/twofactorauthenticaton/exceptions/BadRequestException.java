package com.augusto.twofactorauthenticaton.exceptions;

import java.io.Serializable;

public class BadRequestException extends RuntimeException implements Serializable {

    public BadRequestException(String message) {
        super(message);
    }
}
