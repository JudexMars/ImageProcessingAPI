package org.judexmars.imagecrud.exception;

import org.springframework.http.HttpStatus;

public class InvalidJwtException extends BaseException {

    public InvalidJwtException() {
        super(HttpStatus.UNAUTHORIZED, "exception.invalid_jwt");
    }
}
