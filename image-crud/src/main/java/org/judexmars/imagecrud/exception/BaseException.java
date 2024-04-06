package org.judexmars.imagecrud.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Generic exception to return to the client.
 */
@Getter
public class BaseException extends ResponseStatusException {

  private final String messageCode;

  private final Object[] args;

  /**
   * Default constructor.
   *
   * @param status      error status
   * @param messageCode message code
   * @param args        message arguments
   */
  public BaseException(HttpStatus status, String messageCode, Object... args) {
    super(status);
    this.messageCode = messageCode;
    this.args = args;
  }
}
