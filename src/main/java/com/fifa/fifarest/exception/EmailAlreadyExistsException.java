package com.fifa.fifarest.exception;

public class EmailAlreadyExistsException extends DuplicateResourceException {
    public EmailAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists");
    }
}
