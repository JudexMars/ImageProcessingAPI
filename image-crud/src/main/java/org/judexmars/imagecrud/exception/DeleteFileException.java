package org.judexmars.imagecrud.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a file cannot be deleted.
 */
public class DeleteFileException extends BaseException {

  public DeleteFileException() {
    super(HttpStatus.BAD_REQUEST, "exception.delete_unsuccessful");
  }
}
