package ru.mentee.power.crm.spring.rest.fixed.exception;

public class InvalidStatusException extends RuntimeException {
  public InvalidStatusException(String message) {
    super(message);
  }
}
