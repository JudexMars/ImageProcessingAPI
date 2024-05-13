package org.judexmars.imagecrud.exception;

public class ImageNotFoundException extends BaseNotFoundException {
    public ImageNotFoundException(String link) {
        super("exception.image_not_found", link);
    }

    public ImageNotFoundException(Long id) {
        super("exception.image_not_found", id);
    }
}
