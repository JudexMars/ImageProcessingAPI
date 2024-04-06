package org.judexmars.imagecrud.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an account with the same username already exists.
 */
public class AccountAlreadyExistsException extends BaseException {

  public AccountAlreadyExistsException(String username) {
    super(HttpStatus.CONFLICT, "exception.account_already_exists", username);
  }
}
