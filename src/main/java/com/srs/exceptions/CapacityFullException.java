package com.srs.exceptions;

public class CapacityFullException extends RuntimeException {

  public CapacityFullException(String message) {
    super(message);
  }
}
