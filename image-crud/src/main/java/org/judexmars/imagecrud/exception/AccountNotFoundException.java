package org.judexmars.imagecrud.exception;

/**
 * Thrown when an account is not found.
 */
public class AccountNotFoundException extends BaseNotFoundException {

  public AccountNotFoundException(Object... args) {
    super("exception.account_not_found", args);
  }
}
