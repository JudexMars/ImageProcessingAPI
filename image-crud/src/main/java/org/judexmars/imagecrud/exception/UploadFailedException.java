package org.judexmars.imagecrud.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a file upload fails.
 */
public class UploadFailedException extends BaseException {

  public UploadFailedException() {
    super(HttpStatus.BAD_REQUEST, "exception.upload_failed");
  }
}
