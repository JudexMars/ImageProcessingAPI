package org.judexmars.imagecrud.exception;

public class AccountNotFoundException extends BaseNotFoundException {

    public AccountNotFoundException(Object... args) {
        super("exception.account_not_found", args);
    }
}
