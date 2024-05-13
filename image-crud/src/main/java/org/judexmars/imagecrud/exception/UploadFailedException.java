package org.judexmars.imagecrud.exception;

import org.springframework.http.HttpStatus;

public class UploadFailedException extends BaseException {

    public UploadFailedException() {
        super(HttpStatus.BAD_REQUEST, "exception.upload_failed");
    }
}
