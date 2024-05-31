package org.judexmars.imagecrud.exception;

/**
 * Thrown when request is not found.
 */
public class RequestNotFoundException extends BaseNotFoundException {
  public RequestNotFoundException(String link) {
    super("exception.request_not_found", link);
  }
}
