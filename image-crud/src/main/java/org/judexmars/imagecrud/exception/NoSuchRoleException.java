package org.judexmars.imagecrud.exception;

/**
 * Thrown when a role is not found.
 */
public class NoSuchRoleException extends BaseNotFoundException {


  public NoSuchRoleException(Object... args) {
    super("exception.no_such_role", args);
  }
}
