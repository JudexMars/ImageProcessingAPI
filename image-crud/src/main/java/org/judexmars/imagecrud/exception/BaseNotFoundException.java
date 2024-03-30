package org.judexmars.imagecrud.exception;

import org.springframework.http.HttpStatus;

public class BaseNotFoundException extends BaseException {

    public BaseNotFoundException(String messageCode, Object... args) {
        super(HttpStatus.NOT_FOUND, messageCode, args);
    }

}
