package org.judexmars.imagecrud.exception;

/**
 * Thrown when image is not found.
 */
public class ImageNotFoundException extends BaseNotFoundException {
  public ImageNotFoundException(String link) {
    super("exception.image_not_found", link);
  }

}
