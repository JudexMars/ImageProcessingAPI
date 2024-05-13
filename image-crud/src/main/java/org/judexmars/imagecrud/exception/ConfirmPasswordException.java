package org.judexmars.imagecrud.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when passwords do not match.
 */
public class ConfirmPasswordException extends BaseException {

  public ConfirmPasswordException() {
    super(HttpStatus.BAD_REQUEST, "exception.confirm_password_invalid");
  }
}
